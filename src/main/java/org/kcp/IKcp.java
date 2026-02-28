package org.kcp;

// Imports
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import java.util.List;

public interface IKcp {
    void release();
    ByteBuf mergeRecv();
    int recv(List<ByteBuf> bufList);
    int peekSize();
    boolean canRecv();
    int send(ByteBuf buf);
    int input(ByteBuf data, boolean regular, long current);
    long currentMs(long now);
    long flush(boolean ackOnly, long current);
    void update(long current);
    long check(long current);
    boolean checkFlush();
    int setMtu(int mtu);
    int getInterval();
    int nodelay(boolean nodelay, int interval, int resend, boolean nc);
    int waitSnd();
    long getConv();
    void setConv(long conv);
    User getUser();
    void setUser(User user);
    int getState();
    void setState(int state);
    boolean isNodelay();
    void setNodelay(boolean nodelay);
    void setFastresend(int fastresend);
    void setRxMinrto(int rxMinrto);
    void setRcvWnd(int rcvWnd);
    void setAckMaskSize(int ackMaskSize);
    void setReserved(int reserved);
    int getSndWnd();
    void setSndWnd(int sndWnd);
    boolean isStream();
    void setStream(boolean stream);
    void setByteBufAllocator(ByteBufAllocator byteBufAllocator);
    KcpOutput getOutput();
    void setOutput(KcpOutput output);
    void setAckNoDelay(boolean ackNoDelay);
    int getSrtt();
}