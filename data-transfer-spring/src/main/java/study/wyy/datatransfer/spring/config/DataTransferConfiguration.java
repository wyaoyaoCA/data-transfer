package study.wyy.datatransfer.spring.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import study.wyy.datatransfer.api.service.TaskStorageService;
import study.wyy.datatransfer.spring.component.DataTaskTrigger;
import study.wyy.datatransfer.spring.spi.impl.RedisTaskStorageService;
import study.wyy.datatransfer.spring.executor.DataTransferTaskExecutor;
import study.wyy.datatransfer.spring.executor.DefaultDataTransferTaskExecutor;
import study.wyy.datatransfer.spring.formater.DefaultXlsxDataWriterFactory;
import study.wyy.datatransfer.spring.formater.XlsxDataWriterFactory;
import study.wyy.datatransfer.spring.properties.DataTaskProperties;
import study.wyy.datatransfer.spring.spi.FileManager;
import study.wyy.datatransfer.spring.spi.impl.LocalFileManager;
import study.wyy.datatransfer.spring.spi.impl.LocalGuavaCacheTaskStorageService;
import study.wyy.datatransfer.spring.task.DataExporter;
import study.wyy.datatransfer.spring.task.DataImporter;
import java.io.IOException;

/**
 * @author wyaoyao
 * @date 2021/2/25 10:57
 */
@Configuration
@ComponentScan("study.wyy.datatransfer")
@EnableConfigurationProperties({DataTaskProperties.class})
public class DataTransferConfiguration {

    private final DataTaskProperties dataTaskProperties;

    @Autowired
    public DataTransferConfiguration(DataTaskProperties dataTaskProperties) {
        this.dataTaskProperties = dataTaskProperties;
    }

    @Bean
    public DataTaskTrigger dataTaskTrigger(DataTransferTaskExecutor dataTransferTaskExecutor, TaskStorageService taskStorageService){
        return new DataTaskTrigger(dataTransferTaskExecutor,taskStorageService);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "data.transfer.file.activate", havingValue = "local" ,matchIfMissing = true)
    public FileManager fileManager(){
        String fileBaseDir = dataTaskProperties.getFile().getFileBaseDir();
        return new LocalFileManager(fileBaseDir);
    }


    @Bean
    @ConditionalOnMissingBean
    public XlsxDataWriterFactory xlsxDataWriterFactory(){
       return new DefaultXlsxDataWriterFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public DataTransferTaskExecutor dataTransferTaskExecutor(FileManager fileManager,XlsxDataWriterFactory xlsxDataWriterFactory,TaskStorageService taskStorageService) throws IOException {
        DefaultDataTransferTaskExecutor defaultDataTransferTaskExecutor = new DefaultDataTransferTaskExecutor(fileManager,xlsxDataWriterFactory, taskStorageService);
        return defaultDataTransferTaskExecutor;
    }

    @Bean
    public BeanPostProcessor BeanPostProcessor(DataTransferTaskExecutor dataTransferTaskExecutor){
       return new BeanPostProcessor(){
           @Override
           public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
               if(bean instanceof DataExporter){
                   dataTransferTaskExecutor.registerExporter((DataExporter) bean);
               }
               if (bean instanceof DataImporter){
                   dataTransferTaskExecutor.registerImporter((DataImporter) bean);
               }
               return bean;
           }
       };
    }


    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "data.transfer.task-storage", havingValue = "local" ,matchIfMissing = true)
    public TaskStorageService localTaskStorageService(){
        LocalGuavaCacheTaskStorageService localGuavaCacheTaskStorageService = new LocalGuavaCacheTaskStorageService();
        return localGuavaCacheTaskStorageService;
    }


    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "data.transfer.task-storage", havingValue = "redis")
    @ConditionalOnBean(StringRedisTemplate.class)
    public TaskStorageService redisTaskStorageService(StringRedisTemplate stringRedisTemplate){
        TaskStorageService redisTaskStorageService = new RedisTaskStorageService(stringRedisTemplate);
        return redisTaskStorageService;
    }
}
