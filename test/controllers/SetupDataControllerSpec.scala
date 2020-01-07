/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import models.DataModel
import org.scalatest.BeforeAndAfter
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.mvc.Http.Status
import testUtils.TestSupport

class SetupDataControllerSpec extends TestSupport with BeforeAndAfter {

  before {
    dropTestCollection("data")
  }

  object TestSetupDataController extends SetupDataController(repo, cc)

  "SetupDataController.addData" when {

    "validateUrlMatch returns 'true'" should {

      val model: DataModel = DataModel(
        _id = "1234",
        method = "GET",
        response = Some(Json.parse("{}")),
        status = Status.OK)

      "when the provided JSON is valid" should {

        "return Status OK (200) if data successfully added to stub" in {
          lazy val request = FakeRequest().withBody(Json.toJson(model)).withHeaders(("Content-Type", "application/json"))
          lazy val result = TestSetupDataController.addData(request)
          status(result) shouldBe Status.OK
        }
      }
    }

    "not a GET request" should {

      val model: DataModel = DataModel(
        _id = "1234",
        method = "BLOB",
        response = Some(Json.parse("{}")),
        status = Status.OK)

      "return Status BadRequest (400)" in {
        lazy val request = FakeRequest().withBody(Json.toJson(model)).withHeaders(("Content-Type", "application/json"))
        lazy val result = TestSetupDataController.addData(request)

        status(result) shouldBe Status.BAD_REQUEST
      }

    }

  }

  "SetupDataController.removeData" should {

    "return Status OK (200) on successful removal of data from the stub" in {
      lazy val request = FakeRequest()
      lazy val result = TestSetupDataController.removeData("someUrl")(request)

      TestSetupDataController.removeData("someUrl")

      status(result) shouldBe Status.OK
    }

  }

  "SetupDataController.removeAllData" should {

    "return Status OK (200) on successful removal of all stubbed data" in {
      lazy val request = FakeRequest()
      lazy val result = TestSetupDataController.removeAll()(request)

      TestSetupDataController.removeAll

      status(result) shouldBe Status.OK
    }

  }
}
