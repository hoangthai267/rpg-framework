package com.rpg.framework.handler;

import com.rpg.framework.util.Time;

import io.netty.channel.ChannelFuture;

public class MessageListener implements io.netty.channel.ChannelFutureListener
{
	UserHandler m_owner;
    int m_cmdId;
    int m_msgSize;
    String m_msgName;
    long m_secCmdStart;
    long m_secCmdSendStart;

    public MessageListener(UserHandler Owner,
    		int CommandId,
    		int MsgSize,
    		String MsgName,
    		long SecCmdStart,
    		long SecSendStart
    )
    {
        super();
        m_owner = Owner;
        m_cmdId = CommandId;
        m_msgSize = MsgSize;
        m_secCmdStart = SecCmdStart;
        m_secCmdSendStart = SecSendStart;
        m_msgName = MsgName;
    }

    public void operationComplete(ChannelFuture AFuture) throws Exception
    {
        if (m_owner != null)
        {
            long curSec = Time.currentTimeMillis() - m_secCmdStart;
            System.out.println("Request time of user: " + curSec);
        }
        else
        {
            long curSec = Time.currentTimeMillis() - m_secCmdStart;
            System.out.println("Request time: " + curSec);
        }
    }
}