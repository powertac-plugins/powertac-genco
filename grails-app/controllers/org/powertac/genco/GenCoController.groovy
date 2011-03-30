package org.powertac.genco

import grails.converters.XML

class GenCoController {

  def scaffold = GenCo

  def get = {
    def genCoInstance = GenCo.get(params.id)
    render(contentType: "text/xml", encoding: "UTF-8", text: genCoInstance as XML)
  }
}
