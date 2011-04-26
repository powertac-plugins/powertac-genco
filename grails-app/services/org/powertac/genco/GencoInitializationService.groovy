/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS,  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.powertac.genco

import org.powertac.common.Broker
import org.powertac.common.Competition;
import org.powertac.common.PluginConfig
import org.powertac.common.interfaces.InitializationService

class GencoInitializationService 
    implements InitializationService
{
  static transactional = true
  
  def simpleGencoService

  @Override
  public void setDefaults ()
  {
    build('nsp1', 100, 0.05, 3.0, 8, 1.0)
    build('nsp2', 60, 0.05, 3.8, 8, 1.0)
    build('gas1', 40, 0.03, 5.0, 1, 0.5)
    build('gas2', 30, 0.03, 5.5, 0, 0.5)    
  }

  @Override
  public String initialize (Competition competition, List<String> completedInits)
  {
    simpleGencoService.init()
    return 'Genco'
  }

  GenCo build (String name, BigDecimal nominalCapacity, Double variability, BigDecimal cost,
               Integer commitmentLeadTime, Double carbonEmissionRate)
  {
    GenCo genco = new GenCo(variability: variability, currentCapacity: nominalCapacity)
    PluginConfig config = new PluginConfig(roleName:'genco', name: name,
        configuration: ['nominalCapacity': nominalCapacity.toString(), 'cost': cost.toString(),
                        'commitmentLeadtime': commitmentLeadTime.toString(),
                        'carbonEmissionRate': carbonEmissionRate.toString()])
    config.save()
    genco.config = config
    Broker face = new Broker(username: name, local: true)
    if (!face.validate()) {
      face.errors.allErrors.each {log.error(it.toString())}
    }
    face.save()
    genco.broker = face
    genco.save()
    return genco
  }
}
