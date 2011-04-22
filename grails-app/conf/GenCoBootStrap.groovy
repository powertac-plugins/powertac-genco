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
import org.powertac.common.Broker
import org.powertac.common.PluginConfig
import org.powertac.genco.GenCo
import org.powertac.genco.GencoFactory

/**
 * Genco configuration. Public data goes in the PluginConfig instances, private data goes
 * directly into the GenCo instances.
 * @author John Collins
 */
class GenCoBootStrap {
  
  def simpleGencoService

  def init = { servletContext ->
    // create some GenCo instances
    def factory = new GencoFactory()
    factory.build('nsp1', 100, 0.05, 3.0, 8, 1.0)
    factory.build('nsp2', 60, 0.05, 3.8, 8, 1.0)
    factory.build('gas1', 40, 0.03, 5.0, 1, 0.5)
    factory.build('gas2', 30, 0.03, 5.5, 0, 0.5)

    //simpleGencoService.init()
  }
  def destroy = {
  }
}
