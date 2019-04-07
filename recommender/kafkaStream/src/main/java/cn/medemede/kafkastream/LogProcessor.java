package cn.medemede.kafkastream;

import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;

public class LogProcessor implements Processor<byte[],byte[]> {

    private static final String PREFIX_MSG="abc:";
    private ProcessorContext processorContext;

    @Override
    public void init(ProcessorContext processorContext) {
        this.processorContext=processorContext;
    }

    @Override
    public void process(byte[] key, byte[] value) {
        String ratingValue=new String(value);
        if(ratingValue.contains(PREFIX_MSG)){
            processorContext.forward("log.".getBytes(),ratingValue.split(PREFIX_MSG)[1]);
        }
    }

    @Override
    public void close() {

    }
}
