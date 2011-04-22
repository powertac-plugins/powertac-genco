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
import org.powertac.common.PluginConfig

/**
 * Factory for GenCo instance and its associated parts.
 * @author John Collins
 */
class GencoFactory
{
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
    genco.broker = new Broker(username: name, local: true)
    genco.broker.save()
    genco.save()
    return genco
  }
}
