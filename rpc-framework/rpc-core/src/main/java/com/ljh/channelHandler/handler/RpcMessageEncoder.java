package com.ljh.channelHandler.handler;

import com.ljh.transport.message.MessageFormatConstant;
import com.ljh.transport.message.RequestPayload;
import com.ljh.transport.message.RpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * 出站第一个处理器 自定义编码器 编码 + 序列化
 * @author ljh
 */
public class RpcMessageEncoder extends MessageToByteEncoder<RpcRequest> {
    private static final Logger log = LoggerFactory.getLogger(RpcMessageEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest, ByteBuf byteBuf) throws Exception {


        //4个字节的魔术值
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);

        //1个字节的版本号
        byteBuf.writeByte(MessageFormatConstant.VERSION);

        //2个字节的头部长度
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);

        //不知道body的长度 用writeIndex(写指针记录)
        byteBuf.writerIndex(byteBuf.writerIndex() + 4);

        //3个类型
        byteBuf.writeByte(rpcRequest.getRequestType());
        byteBuf.writeByte(rpcRequest.getSerializeType());
        byteBuf.writeByte(rpcRequest.getCompressType());

        //8字节的请求Id
        byteBuf.writeLong(rpcRequest.getRequestId());
        //写入请求体
        byte[] bodyBytes = getBodyBytes(rpcRequest.getRequestPayload());
        byteBuf.writeBytes(bodyBytes);

        int writerIndex = byteBuf.writerIndex();
        byteBuf.writerIndex(7);

        byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + bodyBytes.length);
        byteBuf.writerIndex(writerIndex);

    }

    /**
     * 获取请求体 字节数组
     * @param requestPayload
     * @return
     */
    private byte[] getBodyBytes(RequestPayload requestPayload) {


        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(requestPayload);
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("请求体序列化异常");
            throw new RuntimeException(e);

        }
    }
}
