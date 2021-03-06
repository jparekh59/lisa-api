/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.lisaapi.models

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class ID(id:String)

case class ReferenceNumber(ref:String)

case class ISO8601Date (date:String)

object ISO8601Date {
  implicit val format = Json.format[ISO8601Date]
}

object Constants {

  protected trait EnumKey

  case object TRANSACTION extends EnumKey
  case object EVENT_TYPE extends EnumKey
  case object CREATION_REASON extends EnumKey
  case object CLAIM_REASON extends EnumKey
  case object CLOSURE_REASON extends EnumKey

  val elements = Map(TRANSACTION.toString -> List("Bonus"),
    EVENT_TYPE.toString -> List("LISA Investor Terminal Ill Health", "LISA Investor Death", "House Purchase"),
    CREATION_REASON.toString -> List("New", "Transferred"),
    CLAIM_REASON.toString -> List("Life Event", "Regular Bonus"),
    CLOSURE_REASON.toString -> List("All funds withdrawn")  )


    def apply (key: Constants.EnumKey, code:String) =
    if (elements.get(key.toString).get.contains(code))  code
    else throw new IllegalArgumentException

}

case class TransactionType (code:String)
{
  Constants(Constants.TRANSACTION,code)
}

case class EventType(typeCode:String) {
  Constants(Constants.EVENT_TYPE,typeCode)
}


case class CreationReason(creationCode: String) {
  Constants(Constants.CREATION_REASON,creationCode)
}

case class ClaimReason(reason: String)  {
  Constants(Constants.CLAIM_REASON,reason)
}

case class ClosureReason(closureCode: String) {
  Constants(Constants.CLOSURE_REASON,closureCode)
}


case class LisaManager(referenceNo: ReferenceNumber, name: String)

case class LisaInvestor(NINO:String, firstName :String, lastName:String, dob:ISO8601Date)

object LisaInvestor {
  implicit val format = Json.format[LisaInvestor]
}

case class LisaAccount(investorID:ID,
                       accountID: AccountId,
                       lisaManagerReferenceNumber:ReferenceNumber,
                       creationReason:CreationReason,
                       firstSubscriptionDate:ISO8601Date,
                       transferAccount:TransferAccount,
                       accountClosureReason:ClosureReason,
                       closureDate:ISO8601Date)

case class TransferAccount(transferredFromAccountID:AccountId, transferredFromLMRN:ReferenceNumber, transferInDate:ISO8601Date)

case class Bonuses(bonusDueForPeriod: Amount, totalBonusDueYTD: Amount, bonusPaidYTD: Option[Amount], claimReason: BonusClaimReason)

object Bonuses {
  implicit val bonusesReads: Reads[Bonuses] = (
    (JsPath \ "bonusDueForPeriod").read[Amount](JsonReads.nonNegativeAmount) and
    (JsPath \ "totalBonusDueYTD").read[Amount](JsonReads.nonNegativeAmount) and
    (JsPath \ "bonusPaidYTD").readNullable[Amount](JsonReads.nonNegativeAmount) and
    (JsPath \ "claimReason").read(JsonReads.bonusClaimReason)
  )(Bonuses.apply _)

  implicit val bonusesWrites: Writes[Bonuses] = (
    (JsPath \ "bonusDueForPeriod").write[Amount] and
    (JsPath \ "totalBonusDueYTD").write[Amount] and
    (JsPath \ "bonusPaidYTD").writeNullable[Amount] and
    (JsPath \ "claimReason").write[String]
  )(unlift(Bonuses.unapply))
}

case class HelpToBuyTransfer(htbTransferInForPeriod: Amount, htbTransferTotalYTD: Amount)

object HelpToBuyTransfer {
  implicit val htbReads: Reads[HelpToBuyTransfer] = (
    (JsPath \ "htbTransferInForPeriod").read[Amount](JsonReads.nonNegativeAmount) and
    (JsPath \ "htbTransferTotalYTD").read[Amount](JsonReads.nonNegativeAmount)
  )(HelpToBuyTransfer.apply _)

  implicit val htbWrites: Writes[HelpToBuyTransfer] = (
    (JsPath \ "htbTransferInForPeriod").write[Amount] and
    (JsPath \ "htbTransferTotalYTD").write[Amount]
  )(unlift(HelpToBuyTransfer.unapply))
}

case class InboundPayments(newSubsForPeriod: Option[Amount], newSubsYTD: Amount, totalSubsForPeriod: Amount, totalSubsYTD: Amount)

object InboundPayments {
  implicit val ibpReads: Reads[InboundPayments] = (
    (JsPath \ "newSubsForPeriod").readNullable[Amount](JsonReads.nonNegativeAmount) and
    (JsPath \ "newSubsYTD").read[Amount](JsonReads.nonNegativeAmount) and
    (JsPath \ "totalSubsForPeriod").read[Amount](JsonReads.nonNegativeAmount) and
    (JsPath \ "totalSubsYTD").read[Amount](JsonReads.nonNegativeAmount)
  )(InboundPayments.apply _)

  implicit val ibpWrites: Writes[InboundPayments] = (
    (JsPath \ "newSubsForPeriod").writeNullable[Amount] and
    (JsPath \ "newSubsYTD").write[Amount] and
    (JsPath \ "totalSubsForPeriod").write[Amount] and
    (JsPath \ "totalSubsYTD").write[Amount]
  )(unlift(InboundPayments.unapply))
}

case class LifeEvent(accountID: AccountId,
                      lisaManagerReferenceNumber:ReferenceNumber,
                      eventType:EventType,
                      eventDate:ISO8601Date
                      )

case class LisaTransaction(accountID: AccountId,
                           lisaManagerReferenceNumber:ReferenceNumber,
                           eventId:ID,
                           periodStartDate:ISO8601Date,
                           periodEndDate:ISO8601Date,
                           transactionType: TransactionType,
                           bonuses: Bonuses,
                           htbTransfer:HelpToBuyTransfer
                            )