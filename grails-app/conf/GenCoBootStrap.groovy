import org.powertac.genco.GenCo

class GenCoBootStrap {
  
  def simpleGencoService

  def init = { servletContext ->
    // create some GenCo instances
    GenCo genco = new GenCo(name: 'nsp1', nominalCapacity: 100, 
                            variability: 0.05, cost: 3.0, commitmentLeadtime: 8)
    genco.save()
    genco = new GenCo(name: 'nsp2', nominalCapacity: 60, 
                      variability: 0.05, cost: 3.8, commitmentLeadtime: 8)
    genco.save()
    genco = new GenCo(name: 'gas1', nominalCapacity: 40, 
                      variability: 0.03, cost: 5.0, commitmentLeadtime: 0)
    genco.save()
    genco = new GenCo(name: 'gas2', nominalCapacity: 30, 
                      variability: 0.03, cost: 5.5, commitmentLeadtime: 0)
    genco.save()

    //simpleGencoService.init()
  }
  def destroy = {
  }
}
