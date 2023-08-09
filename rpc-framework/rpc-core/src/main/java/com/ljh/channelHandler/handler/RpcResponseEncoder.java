package com.ljh.channelHandler.handler;

import com.ljh.compress.Compressor;
import com.ljh.compress.CompressorFactory;
import com.ljh.serialize.Serializer;
import com.ljh.serialize.SerializerFactory;
import com.ljh.transport.message.MessageFormatConstant;
import com.ljh.transport.message.RequestPayload;
import com.ljh.transport.message.RpcRequest;
import com.ljh.transport.message.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * 响应 自定义编码器 编码 + 序列化
 * @author ljh
 */
public class RpcResponseEncoder extends MessageToByteEncoder<RpcResponse> {
    private static final Logger log = LoggerFactory.getLogger(RpcResponseEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse, ByteBuf byteBuf) throws Exception {


        //4个字节的魔术值
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);

        //1个字节的版本号
        byteBuf.writeByte(MessageFormatConstant.VERSION);

        //2个字节的头部长度
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);

        //不知道body的长度 用writeIndex(写指针记录)
        byteBuf.writerIndex(byteBuf.writerIndex() + MessageFormatConstant.FULL_FIELD_LENGTH);

        //3个类型
        byteBuf.writeByte(rpcResponse.getCode());
        byteBuf.writeByte(rpcResponse.getSerializeType());
        byteBuf.writeByte(rpcResponse.getCompressType());

        //8字节的请求Id
        byteBuf.writeLong(rpcResponse.getRequestId());




        //序列化 写入请求体
        //byte[] bodyBytes = getBodyBytes(rpcResponse.getObject());
        Serializer serializer = SerializerFactory.getSerializer(rpcResponse.getSerializeType())
                .getSerializer();
        byte[] bodyBytes = serializer.serialize(rpcResponse.getObject());


        Compressor compressor = CompressorFactory.getCompressor(rpcResponse.getCompressType()).getCompressor();
        bodyBytes = compressor.compress(bodyBytes);





        if (bodyBytes != null){

            byteBuf.writeBytes(bodyBytes);
        }


        int bodyLength = bodyBytes == null ? 0 : bodyBytes.length;
        int writerIndex = byteBuf.writerIndex();
        byteBuf.writerIndex(MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH +
                MessageFormatConstant.HEADER_FIELD_LENGTH);

        byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + bodyLength);
        byteBuf.writerIndex(writerIndex);
        log.info("请求在服务端【{}】已经完成了报文的编码", rpcResponse.getRequestId());


    }

    /**
     * 获取请求体 字节数组
     * @param body
     * @return
     */
    private byte[] getBodyBytes(Object body) {

        if (body == null){


            return null;
        }



        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(body);
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("请求体序列化异常");
            throw new RuntimeException(e);

        }
    }
}
