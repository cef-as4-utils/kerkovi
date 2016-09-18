package controllers

import java.util.Properties

import as4Interceptor.AS4Adapter
import backend.{GenericAS4Corner4, GenericAS4Corner1}


object KerkoviApplicationContext{
  var as4Adapter: AS4Adapter = null
  var genericCorner1: GenericAS4Corner1 = null
  var genericCorner4: GenericAS4Corner4 = null
  var serviceProperties: Properties = null
  var actionProperties: Properties = null
  var myPartyID: String = null
}

