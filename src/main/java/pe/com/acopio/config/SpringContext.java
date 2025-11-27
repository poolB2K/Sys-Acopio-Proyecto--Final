package pe.com.acopio.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Clase utilitaria para acceder al contexto de Spring desde clases no
 * administradas por Spring
 */
@Component
public class SpringContext implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    /**
     * Obtiene el contexto de Spring
     * 
     * @return ApplicationContext
     */
    public static ApplicationContext getContext() {
        return context;
    }

    /**
     * Obtiene un bean del contexto de Spring por tipo
     * 
     * @param beanClass Clase del bean
     * @return Bean del tipo especificado
     */
    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }

    /**
     * Obtiene un bean del contexto de Spring por nombre
     * 
     * @param beanName Nombre del bean
     * @return Bean con el nombre especificado
     */
    public static Object getBean(String beanName) {
        return context.getBean(beanName);
    }
}
