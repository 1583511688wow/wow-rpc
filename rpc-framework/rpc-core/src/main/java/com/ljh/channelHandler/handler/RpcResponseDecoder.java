package com.ljh.channelHandler.handler;

import com.ljh.compress.Compressor;
import com.ljh.compress.CompressorFactory;
import com.ljh.enumeration.RequestType;
import com.ljh.serialize.Serializer;
import com.ljh.serialize.SerializerFactory;
import com.ljh.transport.message.MessageFormatConstant;
import com.ljh.transport.message.RequestPayload;
import com.ljh.transport.message.RpcRequest;
import com.ljh.transport.message.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * 自定义响应解码器
 * @author ljh
 */
public class RpcResponseDecoder extends LengthFieldBasedFrameDecoder {

    private static final Logger log = LoggerFactory.getLogger(RpcResponseDecoder.class);

    public RpcResponseDecoder() {
        /**
         * 找到当前报文的总长度，并截取报文，截取出来的报文进行解析
         */
        super(
                // 找到当前报文的总长度，截取报文，截取出来的报文我们可以去进行解析
                // 最大帧的长度，超过这个maxFrameLength值会直接丢弃
                MessageFormatConstant.MAX_FRAME_LENGTH,
                // 长度的字段的偏移量，
                MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH,
                // 长度的字段的长度
                MessageFormatConstant.FULL_FIELD_LENGTH,
                // todo 负载的适配长度
                -(MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH
                        + MessageFormatConstant.HEADER_FIELD_LENGTH + MessageFormatConstant.FULL_FIELD_LENGTH),
                0
        );
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {

        Object decode = super.decode(ctx, in);
        if (decode instanceof ByteBuf ){

            ByteBuf decode1 = (ByteBuf) decode;

            return decodeFrame(decode1);
        }

        return null;
    }

    private Object decodeFrame(ByteBuf byteBuf) {

        // 1、解析魔数
        byte[] magic = new byte[MessageFormatConstant.MAGIC.length];
        byteBuf.readBytes(magic);
        // 检测魔数是否匹配
        for (int i = 0; i < magic.length; i++) {
            if(magic[i] != MessageFormatConstant.MAGIC[i]){
                throw new RuntimeException("获得的请求不合法！");
            }
        }

        // 2、解析版本号
        byte version = byteBuf.readByte();
        if(version > MessageFormatConstant.VERSION){
            throw new RuntimeException("获得的请求版本不被支持。");
        }

        // 3、解析头部的长度
        short headLength = byteBuf.readShort();

        // 4、解析总长度
        int fullLength = byteBuf.readInt();

        // 5、请求类型
        byte responseCode = byteBuf.readByte();

        // 6、序列化类型
        byte serializeType = byteBuf.readByte();

        // 7、压缩类型
        byte compressType = byteBuf.readByte();

        // 8、请求id
        long requestId = byteBuf.readLong();

        long timeStamp = byteBuf.readLong();



        // 封装
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setCode(responseCode);
        rpcResponse.setCompressType(compressType);
        rpcResponse.setSerializeType(serializeType);
        rpcResponse.setRequestId(requestId);
        rpcResponse.setTimeStamp(timeStamp);


        //todo 心跳
//        if (requestType == RequestType.HEART_BEAT.getId()){
//
//
//            return rpcRequest;
//        }

        int payloadLength = fullLength - headLength;
        byte[] payload = new byte[payloadLength];
        byteBuf.readBytes(payload);


        if (payload != null && payload.length > 0){
            //解压缩
            Compressor compressor = CompressorFactory.getCompressor(compressType).getImpl();
            payload = compressor.decompress(payload);



            //反序列化

            Serializer serializer = SerializerFactory.getSerializer(rpcResponse.getSerializeType())
                    .getImpl();
            Object deserialize = serializer.deserialize(payload, Object.class);
            rpcResponse.setObject(deserialize);
            log.info("请求在客户端端【{}】已经完成了报文的解码", rpcResponse.getRequestId());


        }


        return rpcResponse;


    }
}
