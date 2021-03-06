package edu.neu.coe.scala.numerics

import scala.math.pow

/**
 * @author scalaprof
 */
case class Rational(n: Long, d: Long) extends Fractional[Rational] {
  
  // Pre-conditions
  require(Rational.gcd(math.abs(n),math.abs(d))==1,s"Rational($n,$d): arguments have common factor")
  
  // Operators
  def + (that: Rational): Rational = plus(this,that)
  def + (that: Long): Rational = this + Rational(that)
  def - (that: Rational): Rational = minus(this,that)
  def - (that: Long): Rational = this - Rational(that)
  def unary_-: = negate(this)
  def * (that: Rational): Rational = times(this,that)
  def * (that: Long): Rational = this * Rational(that)
  def / (that: Rational): Rational = this * that.invert
  def / (that: Long): Rational = this / Rational(that)
  def ^ (that: Int): Rational = power(that)
  
  // Members declared in scala.math.Numeric
  def fromInt(x: Int) = Rational.apply(x)
  def minus(x: Rational,y: Rational): Rational = plus(x,negate(y))
  def negate(x: Rational): Rational = Rational(-x.n,x.d)
  def plus(x: Rational,y: Rational): Rational = Rational.normalize(x.n*y.d+x.d*y.n,x.d*y.d)
  def times(x: Rational,y: Rational): Rational = Rational.normalize(x.n*y.n, x.d*y.d)
  def toDouble(x: Rational): Double = x.n*1.0d/x.d
  def toFloat(x: Rational): Float = toDouble(x).toFloat
  def toInt(x: Rational): Int = {val l = toLong(x); if (Rational.longAbs(l)<Int.MaxValue) l.toInt else throw new Exception(s"$x is too big for Int")}
  def toLong(x: Rational): Long = if (x.isWhole) x.n else throw new Exception(s"$x is not Whole")
  
  //Members declared in scala.math.Fractional
  def div(x: Rational,y: Rational): Rational = x/y
  
  // Members declared in scala.math.Ordering
  def compare(x: Rational,y: Rational): Int = minus(x,y).n.signum
  
  // Other methods appropriate to Rational
  def invert = Rational(d,n)
  def isWhole = d==1L
  def isZero = n==0L
  def isUnity = n==1L && isWhole
  def isInfinity = d==0L
  def toInt: Int = toInt(this)
  def toLong: Long = toLong(this)
  def toFloat: Float = toFloat(this)
  def toDouble: Double = toDouble(this)
  def power(x: Int) = Rational(Rational.longPow(n,x),Rational.longPow(d,x))
  def toBigDecimal = BigDecimal(n)/d
  def compare(other: Rational): Int = compare(this,other)
  def toRationalString = s"$n/$d"
  override def toString = if (isWhole) toInt.toString else if (d>100000L || toBigDecimal.isExactDouble) toDouble.toString else toRationalString 
}


object Rational {

	implicit class RationalHelper(val sc: StringContext) extends AnyVal {
		def r(args: Any*): Rational = {
			val strings = sc.parts.iterator
			val expressions = args.iterator
			val sb = new StringBuffer()
			while(strings.hasNext) {
				val s = strings.next
				if (s.isEmpty) {
				  if(expressions.hasNext)
					  sb.append(expressions.next)
          else
            throw new Exception("r: logic error: missing expression")
				}
				else
				  sb.append(s)
			}
			if(expressions.hasNext)
			  throw new Exception(s"r: ignored: ${expressions.next}")
			else
				Rational(sb.toString)
		}
	}
  
  val zero = Rational(0)
  val infinity = zero.invert
  val one = Rational(1)
  val ten = Rational(10)
  
  def apply(x: Int): Rational = apply(x.toLong)
  def apply(x: Long): Rational = new Rational(x,1)
  def apply(x: BigDecimal): Rational = if (x.scale >= 0) {
    val e = BigDecimal.apply(10).pow(x.scale)
    normalize((x * e).toLongExact,e.longValue)
  }
  else
    Rational(x.toLongExact)
  
  def apply(x: String): Rational = {
    val rRat = """^\s*(\d+)\s*(\/\s*(\d+)\s*)?$""".r
    val rDec = """^-?(\d|(\d+,?\d+))*(\.\d+)?(e\d+)?$""".r
    x match {
      // TODO I don't understand why we need this first line -- but it IS necessary
      case rRat(n,_,null) => Rational(n.toLong)
      case rRat(n,_,d) => normalize(n.toLong,d.toLong)
      case rRat(n) => Rational(n.toLong)
      case rDec(w,_,f,null) => Rational(BigDecimal.apply(w+f))
      // TODO implement properly the case where the fourth component is "eN"
      case rDec(w,_,f,e) => println(s"$w$f$e"); val b=BigDecimal.apply(w+f+e); println(s"$b"); Rational(b)
      case _ => throw new Exception(s"invalid rational expression: $x")
    }
  }
  def normalize(n: Long, d: Long) = {
    val g = gcd(math.abs(n),math.abs(d))
    apply(n/g,d/g)
  }
  
import scala.annotation.tailrec
  @tailrec private def gcd(a: Long, b: Long): Long = if (b==0) a else gcd(b, a % b)
  private def longPow(a: Long, b: Int): Long = (1 to b).foldLeft(1L){
    case (r,_) => a*r
    }
  private def longAbs(a: Long): Long = if (a < 0) -a else a

  implicit object RationalNumeric extends RationalIsFractional
  implicit def intToRational(x: Int) = Rational(x)
   
  trait RationalIsFractional extends Fractional[Rational] {
    def plus(x: Rational, y: Rational): Rational = x + y
    def minus(x: Rational, y: Rational): Rational = x - y
    def times(x: Rational, y: Rational): Rational = x * y
    def quot(x: Rational, y: Rational): Rational = x / y
    def negate(x: Rational): Rational = Rational(-x.n,x.d)
    def fromInt(x: Int): Rational = Rational(x)
    def rem(x: Rational, y: Rational): Rational = zero
    def toInt(x: Rational): Int = x.toInt
    def toLong(x: Rational): Long = x.toLong
    def toFloat(x: Rational): Float = x.toFloat
    def toDouble(x: Rational): Double = x.toDouble
    //Members declared in scala.math.Fractional
    def div(x: Rational,y: Rational): Rational = x/y
    
    // Members declared in scala.math.Ordering
    def compare(x: Rational,y: Rational): Int = x.compare(y)
  }

}
