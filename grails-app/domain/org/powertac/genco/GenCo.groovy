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

import org.powertac.common.Broker
import org.powertac.common.PluginConfig;
import org.powertac.common.Shout
import org.powertac.common.Timeslot
import org.powertac.common.MarketPosition
import org.powertac.common.enumerations.BuySellIndicator
import org.powertac.common.enumerations.ProductType

/**
 * Represents a producer of power in the transmission domain. Individual
 * models are players on the wholesale side of the Power TAC day-ahead
 * market.
 * @author jcollins
 */
class GenCo
{
  /** Public config data */
  PluginConfig config
  
  /** Current capacity of this producer in mW */
  Double currentCapacity
  
  /** Per-timeslot variability */
  Double variability = 0.01
  
  /** Mean-reversion tendency - portion of variability to revert
   *  back to nominal capacity */
  Double meanReversion = 0.2
  
  /** True if plant is currently operating */
  Boolean inOperation = true
  
  /** Proportion of time plant is working */
  Double reliability = 0.98
  
  /** True if this is a renewable source */
  Boolean renewable = false
  
  Broker broker // dummy broker for mkt interaction
  
  static hasMany = [commitments: MarketPosition] 
  
  static constraints = {
    config(nullable: false)
    broker(nullable: false)
  }

  // public configured parameters  
  String name
  BigDecimal nominalCapacity = 100.0
  BigDecimal cost = 1.0
  Integer commitmentLeadtime = 1
  Double carbonEmissionRate = 0.0 

  /**
   * Updates this model for the current timeslot, by adjusting
   * capacity, checking for downtime, and creating exogenous
   * commitments.
   */
  void updateModel (Random gen, Instant currentTime)
  {
    log.info "Update ${name}"
    updateCapacity(gen.nextDouble())
    updateInOperation(gen.nextDouble())
  }
  
  /**
   * Generates Shouts in the market to sell available capacity
   */
  void generateShouts (Random gen, Instant now, 
                       List<Timeslot> openSlots,
                       auctionService)
  {
    log.info "Generate shouts for $name"
    openSlots?.each { slot ->
      double availableCapacity = currentCapacity
      MarketPosition posn = MarketPosition.findByBrokerAndTimeslot(broker, slot)
      if (posn != null) {
        // posn.overallBalance is negative if we have sold power in this slot
        availableCapacity += posn.overallBalance
      }
      if (availableCapacity > 0.0) {
        // make an offer to sell
        Shout offer =
            new Shout(broker: broker, timeslot: slot,
                      product: ProductType.Future,
                      buySellIndicator: BuySellIndicator.SELL,
                      quantity: availableCapacity,
                      limitPrice: cost)
        //offer.save()
        //broker.addToShouts(offer)
        //broker.save()
	log.debug "${name} offers ${availableCapacity} in ${slot} for ${cost}"
        auctionService?.receiveMessage(offer)
      }
    }
  }

  private void updateCapacity (double val)
  {
    if (variability > 0.0) {
      currentCapacity = 
          currentCapacity +
          nominalCapacity * (val * 2 * variability - variability) + 
          variability * meanReversion * (nominalCapacity - currentCapacity)
    }
  }
  
  private void updateInOperation (double val)
  {
    if (val > reliability) {
      inOperation = false
    }
    else {
      inOperation = true
    }
  }

  // ------------------ configuration access methods -------------------
  void configure (PluginConfig config)
  {
    this.config = config
    name = config.name
    nominalCapacity = 
      getValidConfig(config, 'nominalCapacity', 
                     nominalCapacity.toString()).toBigDecimal()
    cost =  
      getValidConfig(config, 'cost', cost.toString()).toBigDecimal()
    commitmentLeadtime = 
      getValidConfig(config, 'commitmentLeadtime', 
                     commitmentLeadtime.toString()).toInteger()
    carbonEmissionRate = 
      getValidConfig(config, 'carbonEmissionRate', 
                     carbonEmissionRate.toString()).toDouble()
  }
  
  String getValidConfig (PluginConfig config, String name, String defaultValue)
  {
    String result = config.configuration[name]
    if (result == null) {
      log.error "No config value for ${name}: using ${defaultValue}"
      return defaultValue
    }
    else {
      return result
    }
  }
}
