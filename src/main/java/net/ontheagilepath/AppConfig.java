package net.ontheagilepath;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;

@Configuration
@EnableSpringConfigured
//@EnableAspectJAutoProxy
@EnableAspectJAutoProxy(proxyTargetClass=true)
public class AppConfig {
}
