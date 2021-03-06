package com.gupao.example.redisnetworkmodel.multireactor;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 咕泡学院，只为更好的你
 * 咕泡学院-Mic: 2082233439
 * http://www.gupaoedu.com
 **/
public class Reactor implements Runnable{

    private final Selector selector;

    private ConcurrentLinkedQueue<AsyncHandler> events=new ConcurrentLinkedQueue<>();

    public Reactor() throws IOException {
        this.selector = Selector.open();
    }

    public Selector getSelector() {
        return selector;
    }

    @Override
    public void run() {
        while(!Thread.interrupted()){
            AsyncHandler handler;
            try {
                while((handler=events.poll())!=null){ //可以
                    handler.getChannel().configureBlocking(false);
                    SelectionKey selectionKey=handler.getChannel().register(selector,SelectionKey.OP_READ);
                    selectionKey.attach(handler);
                    handler.setSk(selectionKey);
                }
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while(iterator.hasNext()){
                    SelectionKey key=iterator.next();
                    Runnable runnable=(Runnable) key.attachment(); //得到Acceptor实例
                    if(runnable!=null){
                        runnable.run();
                    }
                    iterator.remove();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void register(AsyncHandler handler){
        events.offer(handler); //有一个事件注册
        selector.wakeup();
    }
}
