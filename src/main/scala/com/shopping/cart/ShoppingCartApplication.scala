package com.shopping.cart

import cats.effect.IO
import com.shopping.cart.domain.model._
import com.shopping.cart.domain.service.ShoppingService
import com.shopping.cart.domain.service.implementations.ConfigurableShoppingService
import com.shopping.cart.integration.PricingService
import com.shopping.cart.integration.implementations.CachedPricingService

import scala.annotation.tailrec

object ShoppingCartApplication {
  private val pricingService: PricingService = new CachedPricingService
  private val shoppingService: ShoppingService = new ConfigurableShoppingService(Configuration(tax = 12.5))
  type Quantity = Int
  type ProductPrice = PositiveSmallDecimal

  def buy(products: (ProductName, Quantity)*): IO[Receipt] = {
    for {
      shoppingCart <- addProductsToCart(products.toList, cart = IO.pure(ShoppingCart(Seq.empty)))
      receipt <- IO.fromEither(shoppingService.checkout(shoppingCart))
    } yield receipt
  }

  @tailrec
  private def addProductsToCart(products: List[(ProductName, Quantity)], cart: IO[ShoppingCart]): IO[ShoppingCart] = {
    products match {
      case Nil => cart
      case (productName, quantity) :: t =>
        val newCart = addProduct(productName, quantity, cart)
        addProductsToCart(t, newCart)
    }
  }

  private def addProduct(productName: ProductName, quantity: Quantity, toCart: IO[ShoppingCart]): IO[ShoppingCart] = {
    for {
      _ <- IO.raiseUnless(quantity > 0)(new IllegalArgumentException(s"Quantity must be bigger than 0, but was $quantity")) //TODO: see if this can be removed
      cart: ShoppingCart <- toCart
      price: ProductPrice <- pricingService.priceFor(productName)
      newCart: ShoppingCart <- IO.fromEither(shoppingService.add(ShoppingProduct(productName, price), quantity, cart))
    } yield newCart
  }

}
