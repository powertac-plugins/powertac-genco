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
 
import org.joda.time.Instant

import org.powertac.common.Timeslot
import org.powertac.common.interfaces.TimeslotPhaseProcessor

class SimpleGencoService
    implements TimeslotPhaseProcessor
{

  static transactional = true
  
  def timeService // autowire
  def randomSeedService // autowire
  def competitionControlService
  def auctionService
  
  Random randomGen = null

  void init()
  {
    competitionControlService.registerTimeslotPhase(this, 1)
  }
  
  void activate(Instant now, int phase)
  {
    log.info "Activate"
    Random gen = ensureRandomSeed()
    List<GenCo> gencoList = GenCo.list() 
    gencoList*.updateModel(gen, now)
    List<Timeslot> openSlots = Timeslot.enabledTimeslots()
    gencoList*.generateShouts(gen, now, openSlots, auctionService)
  }
  
  private Random ensureRandomSeed ()
  {
    if (randomGen == null) {
      long randomSeed = randomSeedService.nextSeed('SimpleGencoService', 'genco', 'model')
      randomGen = new Random(randomSeed)
    }
    return randomGen
  }
}
