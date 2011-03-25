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
import org.powertac.common.Shout
import org.powertac.common.Timeslot
import org.powertac.common.MarketPosition
import org.powertac.common.enumerations.BuySellIndicator

/**
 * Represents a producer of power in the transmission domain. Individual
 * models are players on the wholesale side of the Power TAC day-ahead
 * market.
 * @author jcollins
 */
class GenCo
{
  /** Name of this GenCo */
  String name
  
  /** Nominal capacity of this producer in mW */
  Double nominalCapacity = 10.0
  
  /** Current capacity of this producer in mW */
  Double currentCapacity = nominalCapacity
  
  /** Per-timeslot variability */
  Double variability = 0.01
  
  /** Mean-reversion tendency - portion of variability to revert
   * back to nominal capacity */
  Double meanReversion = 0.2
  
  /** Cost per mWh */
  BigDecimal cost = 4.0
  
  /** True if plant is currently operating */
  Boolean inOperation = true
  
  /** Proportion of time plant is working */
  Double reliability = 0.98
  
  /** Leadtime for commitment, in hours */
  Integer commitmentLeadtime = 0
  
  /** Carbon emission rate in kg/kWh */
  BigDecimal carbonEmissionRate = 0.5
  
  /** True if this is a renewable source */
  Boolean renewable = false
  
  Broker broker
  
  static hasMany = [commitments: MarketPosition] 
  
  static constraints = {
    name(nullable: false)
  }

  /**
   * Updates this model for the current timeslot, by adjusting
   * capacity, checking for downtime, and creating exogenous
   * commitments.
   */
  void updateModel (Random gen, Instant currentTime)
  {
    updateCapacity(gen.nextDouble())
    updateInOperation(gen.nextDouble())
  }
  
  /**
   * Generates Shouts in the market to sell available capacity
   */
  void generateBids (Random gen, Instant now, List<Timeslot> openSlots)
  {
    ensureBroker()
    openSlots.each { slot ->
      MarketPosition posn = MarketPosition.findByBrokerAndTimeslot(broker, slot)
      // posn.overallBalance is negative if we have sold power in this slot
      double availableCapacity = currentCapacity + posn.overallBalance
      if (availableCapacity > 0.0) {
        // make an offer to sell
        Shout offer =
            new Shout(broker: broker, timeslot: slot,
                      buySellIndicator: BuySellIndicator.SELL,
                      quantity: availableCapacity,
                      limitPrice: cost)
        shout.save()
        broker.addToShouts(shout)
        broker.save()
        // TODO - send to market somehow
      }
    }
  }
  
  private void ensureBroker ()
  {
    if (broker == null) {
      broker = new Broker(name: name, local: true)
      broker.save()
      this.save()
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
    if (val < reliability) {
      inOperation = false
    }
    else {
      inOperation = true
    }
  }
}
