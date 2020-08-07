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

package repositories

import javax.inject.{Inject, Singleton}
import models.SchemaModel
import play.api.libs.json.Json.JsValueWrapper
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.DefaultDB
import reactivemongo.api.commands._
import uk.gov.hmrc.mongo.MongoConnector

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SchemaRepository @Inject()(reactiveMongoComponent: ReactiveMongoComponent)
                                (implicit ec: ExecutionContext) {

  lazy val mongoConnector: MongoConnector = reactiveMongoComponent.mongoConnector
  implicit val db: () => DefaultDB = mongoConnector.db

  lazy val repository = new DynamicStubRepository[SchemaModel]

  def find(query: (String, JsValueWrapper)*): Future[List[SchemaModel]] = repository.find(query:_*)

  def insert(schema: SchemaModel): Future[WriteResult] = repository.insert(schema)

  def removeById(id: String): Future[WriteResult] = repository.removeById(id)

  def removeAll(): Future[WriteResult] = repository.removeAll()
}
