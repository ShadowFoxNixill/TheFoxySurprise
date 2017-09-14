package net.nixill.dicecalc.discord;

import com.vdurmont.emoji.Emoji;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IEmoji;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.RequestBuffer;
import sx.blah.discord.util.RequestBuffer.IRequest;
import sx.blah.discord.util.RequestBuffer.RequestFuture;

/**
 * A class with convenience methods for sending, editing, deleting, or reacting
 * to messages using Discord4J's {@link RequestBuffer} system.
 * 
 * @author Nixill
 */
public class MessageSender {
  /**
   * Makes a request to send a message to a channel.
   * 
   * @param chan
   *          The channel to which the message should be sent.
   * @param msg
   *          The message to send.
   * @return A {@link RequestFuture} that will eventually hold the sent message.
   */
  public static RequestFuture<IMessage> send(IChannel chan, String msg) {
    return send(chan, msg, new IRequest<IMessage>() {
      @Override
      public IMessage request() {
        return null;
      }
    });
  }
  
  /**
   * Makes a request to send a message to a channel. If the bot isn't permitted
   * to send the message in the channel, it attempts to PM the exact same
   * message to the specified user.
   * 
   * @param chan
   *          The channel to which the message should be sent.
   * @param msg
   *          The message to send.
   * @param user
   *          The user to which the message should be PM'd if it can't be sent
   *          in <code>chan</code>.
   * @return A {@link RequestFuture} that will eventually hold the sent message.
   */
  public static RequestFuture<IMessage> send(IChannel chan, String msg, IUser user) {
    return send(chan, msg, new IRequest<IMessage>() {
      @Override
      public IMessage request() {
        return user.getOrCreatePMChannel().sendMessage(msg);
      }
    });
  }
  
  /**
   * Makes a request to send a message to a channel. If the bot isn't permitted
   * to send the message in the channel, it runs <code>pmRun</code> instead.
   * 
   * @param chan
   *          The channel to which the message should be sent.
   * @param msg
   *          The message to send.
   * @param pmRun
   *          The request to be performed if the bot can't send messages in
   *          <code>chan</code>.
   * @return A {@link RequestFuture} that will eventually hold the sent message.
   */
  public static RequestFuture<IMessage> send(IChannel chan, String msg, RequestBuffer.IRequest<IMessage> pmRun) {
    return RequestBuffer.request(new IRequest<IMessage>() {
      @Override
      public IMessage request() throws RateLimitException {
        IChannel chnl = chan;
        try {
          String message = msg;
          return chnl.sendMessage(message);
        } catch (DiscordException ex) {
          // do nothing, since I can't see stack traces and if something's going
          // wrong the bot can't pm me
        } catch (MissingPermissionsException ex) {
          return pmRun.request();
        }
        return null;
      }
    });
  }
  
  /**
   * Makes a request to send an {@link EmbedObject} to a channel.
   * 
   * @param chan
   *          The channel to which the embed should be sent.
   * @param msg
   *          The embed to send.
   * @return A {@link RequestFuture} that will eventually hold the sent message.
   */
  public static RequestFuture<IMessage> send(IChannel chan, EmbedObject msg) {
    return send(chan, msg, new IRequest<IMessage>() {
      @Override
      public IMessage request() {
        return null;
      }
    });
  }
  
  /**
   * Makes a request to send an {@link EmbedObject} to a channel. If the bot
   * isn't permitted to send the message in the channel, it attempts to PM the
   * exact same EmbedObject to the specified user.
   * 
   * @param chan
   *          The channel to which the message should be sent.
   * @param msg
   *          The message to send.
   * @param user
   *          The user to which the message should be PM'd if it can't be sent
   *          in <code>chan</code>.
   * @return A {@link RequestFuture} that will eventually hold the sent message.
   */
  public static RequestFuture<IMessage> send(IChannel chan, EmbedObject msg, IUser user) {
    return send(chan, msg, new IRequest<IMessage>() {
      @Override
      public IMessage request() {
        return user.getOrCreatePMChannel().sendMessage(msg);
      }
    });
  }
  
  /**
   * Makes a request to send a message to a channel. If the bot isn't permitted
   * to send the message in the channel, it runs <code>pmRun</code> instead.
   * 
   * @param chan
   *          The channel to which the message should be sent.
   * @param msg
   *          The message to send.
   * @param pmRun
   *          The request to be performed if the bot can't send messages in
   *          <code>chan</code>.
   * @return A {@link RequestFuture} that will eventually hold the sent message.
   */
  public static RequestFuture<IMessage> send(IChannel chan, EmbedObject msg, RequestBuffer.IRequest<IMessage> pmRun) {
    return RequestBuffer.request(new IRequest<IMessage>() {
      @Override
      public IMessage request() throws RateLimitException {
        IChannel chnl = chan;
        try {
          EmbedObject message = msg;
          return chnl.sendMessage(message);
        } catch (DiscordException ex) {
          // do nothing, since I can't see stack traces and if something's going
          // wrong the bot can't pm me
        } catch (MissingPermissionsException ex) {
          return pmRun.request();
        }
        return null;
      }
    });
  }
  
  /**
   * /**
   * Makes a request to send a message with both a string and a
   * {@link EmbedObject} to a channel.
   * 
   * @param chan
   *          The channel to which the embed should be sent.
   * @param msgStr
   *          The string to send.
   * @param msgEm
   *          The embed to send.
   * @return A {@link RequestFuture} that will eventually hold the sent message.
   */
  public static RequestFuture<IMessage> send(IChannel chan, String msgStr, EmbedObject msgEm) {
    return send(chan, msgStr, msgEm, new IRequest<IMessage>() {
      @Override
      public IMessage request() {
        return null;
      }
    });
  }
  
  /**
   * Makes a request to send an {@link EmbedObject} to a channel. If the bot
   * isn't permitted to send the message in the channel, it attempts to PM the
   * exact same EmbedObject to the specified user.
   * 
   * @param chan
   *          The channel to which the message should be sent.
   * @param msgStr
   *          The string to send.
   * @param msgEm
   *          The embed to send.
   * @param user
   *          The user to which the message should be PM'd if it can't be sent
   *          in <code>chan</code>.
   * @return A {@link RequestFuture} that will eventually hold the sent message.
   */
  public static RequestFuture<IMessage> send(IChannel chan, String msgStr, EmbedObject msgEm, IUser user) {
    return send(chan, msgStr, msgEm, new IRequest<IMessage>() {
      @Override
      public IMessage request() {
        return user.getOrCreatePMChannel().sendMessage(msgStr, msgEm);
      }
    });
  }
  
  /**
   * Makes a request to send a message to a channel. If the bot isn't permitted
   * to send the message in the channel, it runs <code>pmRun</code> instead.
   * 
   * @param chan
   *          The channel to which the message should be sent.
   * @param msgStr
   *          The string to send.
   * @param msgEm
   *          The embed to send.
   * @param pmRun
   *          The request to be performed if the bot can't send messages in
   *          <code>chan</code>.
   * @return A {@link RequestFuture} that will eventually hold the sent message.
   */
  public static RequestFuture<IMessage> send(IChannel chan, String msgStr, EmbedObject msgEm,
      RequestBuffer.IRequest<IMessage> pmRun) {
    return RequestBuffer.request(new IRequest<IMessage>() {
      @Override
      public IMessage request() throws RateLimitException {
        IChannel chnl = chan;
        try {
          return chnl.sendMessage(msgStr, msgEm);
        } catch (DiscordException ex) {
          // do nothing, since I can't see stack traces and if something's going
          // wrong the bot can't pm me
        } catch (MissingPermissionsException ex) {
          return pmRun.request();
        }
        return null;
      }
    });
  }
  
  public static void sendMultiple(IChannel chan, String... msgs) {
    Object timer = new Object();
    synchronized (timer) {
      for (String msg : msgs) {
        send(chan, msg);
        try {
          timer.wait(2000);
        } catch (InterruptedException e) {
          // do nothing
        }
      }
    }
  }
  
  /**
   * Attempts to send multiple {@link EmbedObject}s to a channel.
   * 
   * @param chan
   *          The channel to which the EmbedObjects should be sent.
   * @param msgs
   *          The EmbedObjects to send.
   */
  public static void sendMultiple(IChannel chan, EmbedObject... msgs) {
    Object timer = new Object();
    synchronized (timer) {
      for (EmbedObject msg : msgs) {
        send(chan, msg);
        try {
          timer.wait(2000);
        } catch (InterruptedException e) {
          // do nothing
        }
      }
    }
  }
  
  /**
   * Attempts to edit an existing message.
   * 
   * @param msg
   *          The message to update.
   * @param out
   *          The new message.
   * @return A {@link RequestFuture} that will eventually hold the new message.
   */
  public static RequestFuture<IMessage> update(IMessage msg, String out) {
    return RequestBuffer.request(new IRequest<IMessage>() {
      public IMessage request() throws RateLimitException {
        String newMessage = out;
        return msg.edit(newMessage);
      }
    });
  }
  
  /**
   * Attempts to edit an existing message.
   * 
   * @param msg
   *          The message to update.
   * @param out
   *          The new {@link EmbedObject}.
   * @return A {@link RequestFuture} that will eventually hold the new message.
   */
  public static RequestFuture<IMessage> update(IMessage msg, EmbedObject out) {
    return RequestBuffer.request(new IRequest<IMessage>() {
      public IMessage request() throws RateLimitException {
        EmbedObject newMessage = out;
        return msg.edit(newMessage);
      }
    });
  }
  
  /**
   * Attempts to delete an existing message.
   * 
   * @param msg
   *          The message to delete.
   */
  public static void delete(IMessage msg) {
    RequestBuffer.request(() -> {
      IUser us = DiscordBot.getClient().getOurUser();
      if (msg != null) {
        if (msg.getAuthor().equals(us)) {
          msg.delete();
        } else {
          IChannel chan = msg.getChannel();
          if (chan.isPrivate()) return;
          if (chan.getModifiedPermissions(us).contains(Permissions.MANAGE_MESSAGES)) msg.delete();
        }
      }
    });
  }
  
  /**
   * Attempts to react to an existing message with an emoji.
   * 
   * @param msg
   *          The message to which to react.
   * @param emo
   *          The emoji with which to react.
   */
  public static void react(IMessage msg, Emoji emo) {
    RequestBuffer.request(() -> {
      msg.addReaction(emo);
    });
  }
  
  /**
   * Attempts to react to an existing message with an emoji.
   * 
   * @param msg
   *          The message to which to react.
   * @param emo
   *          The emoji with which to react.
   */
  public static void react(IMessage msg, IEmoji emo) {
    RequestBuffer.request(() -> {
      msg.addReaction(emo);
    });
  }
}
