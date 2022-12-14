package com.shopping.cart.domain.model

import enumeratum._

sealed trait ProductName extends EnumEntry

object ProductName extends Enum[ProductName] with CirceEnum[ProductName] {

  val values: IndexedSeq[ProductName] = findValues

  case object Cheerios extends ProductName
  case object Cornflakes extends ProductName
  case object Frosties extends ProductName
  case object Shreddies extends ProductName
  case object Weetabix extends ProductName
}

case class ShoppingProduct(name: ProductName, price: PositiveSmallDecimal)
