package org.powertac.genco

import grails.test.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Instant
import org.powertac.common.MarketPosition
import org.powertac.common.Shout
import org.powertac.common.Timeslot
import org.powertac.common.TimeService

class GencoServiceTests extends GrailsUnitTestCase 
{
  def simpleGencoService
  def gencoInitializationService
  def timeService
  
  Instant start
  
  GenCo genco

  protected void setUp() 
  {
    super.setUp()
    genco = gencoInitializationService.build('MunicipalPower', 10, 0.01, 3.0, 8, 1.0)
    if (!genco.validate()) {
      genco.errors.allErrors.each { println it.toString() }
    }
    start = new DateTime(2011, 1, 1, 12, 0, 0, 0, DateTimeZone.UTC).toInstant()
    timeService.setCurrentTime(start)
  }

  protected void tearDown() 
  {
    super.tearDown()
  }

  void testGenco() 
  {
    assertNotNull("created GenCo", genco)
    assertEquals("correct name", 'MunicipalPower', genco.name)
  }

  // reduce capacity by 1%  
  void testGencoUpdate1 ()
  {
    def values = [0.0, 0.5]
    def mockRandom = [nextDouble: { -> 
      def result = values.first()
      values = values.tail()
      return result }] as Random
    assertEquals("initial capacity", 10.0, genco.currentCapacity, 1e-6)
    assertTrue("in operation", genco.inOperation)
    genco.updateModel(mockRandom, start)
    assertEquals("current capacity", 9.9, genco.currentCapacity, 1e-6)
    assertTrue("still in operation", genco.inOperation)
  }

  // increase capacity by 1%, put offline  
  void testGencoUpdate2 ()
  {
    def values = [1.0, 0.99]
    def mockRandom = [nextDouble: { -> 
      def result = values.first()
      values = values.tail()
      return result }] as Random
    assertTrue("in operation", genco.inOperation)
    genco.updateModel(mockRandom, start)
    assertEquals("current capacity", 10.1, genco.currentCapacity, 1e-6)
    assertFalse("shut down", genco.inOperation)
  }
  
  // generate shouts
  void testGencoShout ()
  {
    // mock auctioneer
    def shouts = []
    def mockAuctioneer = [processShout: {shout ->
      shouts << shout
    }]
    
    // some timeslots
    long now = timeService.currentTime.millis
    List<Timeslot> slots = []
    def ts = new Timeslot(serialNumber: 3,
                          startInstant: new Instant(now + timeService.HOUR),
                          endInstant: new Instant(now + TimeService.HOUR * 2), enabled: true)
    assert(ts.save())
    slots << ts
    ts = new Timeslot(serialNumber: 4,
                      startInstant: new Instant(now + TimeService.HOUR * 2),
                      endInstant: new Instant(now + TimeService.HOUR * 3), 
                      enabled: true)
    assert(ts.save())
    slots << ts
    ts = new Timeslot(serialNumber: 5,
                      startInstant: new Instant(now + TimeService.HOUR * 3),
                      endInstant: new Instant(now + TimeService.HOUR * 4), 
                      enabled: true)
    assert(ts.save())
    slots << ts
    ts = new Timeslot(serialNumber: 6,
                      startInstant: new Instant(now + TimeService.HOUR * 4),
                      endInstant: new Instant(now + TimeService.HOUR * 5), 
                      enabled: true)
    assert(ts.save())
    slots << ts
    
    // create a non-empty market position for the third timeslot
    MarketPosition posn =
      new MarketPosition(broker: genco.broker, timeslot: slots[2], overallBalance: -4.2)
    assert posn.save()
    
    genco.generateShouts(null, timeService.currentTime, slots, mockAuctioneer)
    assertEquals("four shouts", 4, shouts.size())
    Shout s1 = Shout.findByBrokerAndTimeslot(genco.broker, slots[1])
    assertNotNull("found shout for 2nd slot", s1)
    assertEquals("correct quantity", genco.currentCapacity, s1.quantity)
    assertEquals("correct price", genco.cost, s1.limitPrice)
    Shout s2 = Shout.findByBrokerAndTimeslot(genco.broker, slots[2])
    assertNotNull("found shout for 3rd slot", s2)
    assertEquals("correct quantity", genco.currentCapacity - 4.2, s2.quantity)
    assertEquals("correct price", genco.cost, s2.limitPrice)
  }
}
