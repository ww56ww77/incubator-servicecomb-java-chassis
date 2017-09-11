/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.provider.pojo.reference;

import java.lang.reflect.Proxy;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import io.servicecomb.core.provider.CseBeanPostProcessor.EmptyBeanPostProcessor;
import io.servicecomb.foundation.common.exceptions.ServiceCombException;
import io.servicecomb.provider.pojo.Invoker;

public class PojoReferenceMeta implements FactoryBean<Object>, InitializingBean, EmptyBeanPostProcessor {
  // 原始数据
  private String microserviceName;

  private String schemaId;

  private Class<?> consumerIntf;

  // 根据intf创建出来的动态代理
  // TODO:未实现本地优先(本地场景下，应该跳过handler机制)
  private Object proxy;

  public Object getProxy() {
    return getObject();
  }

  @Override
  public Object getObject() {
    return proxy;
  }

  @Override
  public Class<?> getObjectType() {
    return consumerIntf;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  public void setConsumerIntf(Class<?> intf) {
    this.consumerIntf = intf;
  }

  public void setMicroserviceName(String microserviceName) {
    this.microserviceName = microserviceName;
  }

  public void setSchemaId(String schemaId) {
    this.schemaId = schemaId;
  }

  @Override
  public void afterPropertiesSet() {
    if (consumerIntf == null) {
      throw new ServiceCombException(
          String.format(
              "microserviceName=%s, schemaid=%s, \n"
                  + "do not support implicit interface anymore, \n"
                  + "because that caused problems:\n"
                  + "  1.the startup process relies on other microservices\n"
                  + "  2.cyclic dependent microservices can not be deployed\n"
                  + "suggest to use @RpcReference or "
                  + "<cse:rpc-reference id=\"...\" microservice-name=\"...\" schema-id=\"...\" interface=\"...\"></cse:rpc-reference>.",
              microserviceName,
              schemaId));
    }

    Invoker invoker = new Invoker(microserviceName, schemaId, consumerIntf);
    proxy = Proxy.newProxyInstance(consumerIntf.getClassLoader(), new Class<?>[] {consumerIntf}, invoker);
  }
}