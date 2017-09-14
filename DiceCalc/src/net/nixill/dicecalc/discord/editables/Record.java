package net.nixill.dicecalc.discord.editables;

import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.RequestBuffer.RequestFuture;

public class Record extends Cyclable {
  private IMessage                originalMsg;
  private RequestFuture<IMessage> responseMsgRequest;
  private IMessage                responseMsg;
  private long                    seed;
  
  public IMessage getOriginalMsg() {
    return originalMsg;
  }
  
  public void setOriginalMsg(IMessage originalMsg) {
    this.originalMsg = originalMsg;
  }
  
  public void setResponseMsgReq(RequestFuture<IMessage> responseMsg) {
    this.responseMsgRequest = responseMsg;
  }
  
  public IMessage getResponseMsg() {
    if (responseMsg == null) {
      responseMsg = responseMsgRequest.get();
      responseMsgRequest = null;
    }
    return responseMsg;
  }
  
  public long getSeed() {
    return seed;
  }
  
  public void setSeed(long seed) {
    this.seed = seed;
  }
}
