package rest

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import persistence.entities.Supplier
import persistence.JsonProtocol
import JsonProtocol._
import SprayJsonSupport._
import scala.concurrent.Future
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.ValidationRejection

class RoutesSpec extends AbstractRestTest {

  def actorRefFactory = system
  val modules = new Modules {}
  val suppliers = new SupplierRoutes(modules)

  "Supplier Routes" should {

    "return an empty array of suppliers" in {
     modules.suppliersDal.findById(1) returns Future(None)

      Get("/supplier/1") ~> suppliers.routes ~> check {
        handled shouldEqual true
        status shouldEqual NotFound
      }
    }

    "return an empty array of suppliers when ask for supplier Bad Request when the supplier is < 1" in {
      Get("/supplier/0") ~> suppliers.routes ~> check {
        handled shouldEqual false
        rejection shouldEqual ValidationRejection("The supplier id should be greater than zero", None)
      }
    }

    "return an array with 1 suppliers" in {
      modules.suppliersDal.findById(1) returns Future(Some(Supplier("name 1", "desc 1")))
      Get("/supplier/1") ~> suppliers.routes ~> check {
        handled shouldEqual true
        status shouldEqual OK
        responseAs[Option[Supplier]].isEmpty shouldEqual false
      }
    }

    "create a supplier with the json in post" in {
      modules.suppliersDal.insert(Supplier("name 1","desc 1")) returns  Future(1)
      Post("/supplier",Supplier("name 1","desc 1")) ~> suppliers.routes ~> check {
        handled shouldEqual true
        status shouldEqual Created
      }
    }

    "not handle the invalid json" in {
      Post("/supplier","{\"name\":\"1\"}") ~> suppliers.routes ~> check {
        handled shouldEqual false
      }
    }

    "not handle an empty post" in {
      Post("/supplier") ~> suppliers.routes ~> check {
        handled shouldEqual false
      }
    }

  }

}