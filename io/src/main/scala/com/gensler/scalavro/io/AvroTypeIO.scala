package com.gensler.scalavro.io

import com.gensler.scalavro.error._
import com.gensler.scalavro.types.{AvroType, AvroPrimitiveType}

import org.apache.avro.generic.GenericData

import scala.util.{Try, Success, Failure}
import java.io.{InputStream, OutputStream}

trait AvroTypeIO[T] {

  /**
    * Returns the corresponding AvroType to this AvroTypeIO wrapper.
    */
  def avroType: AvroType[T]

  /**
    * ...
    */
  def asGeneric(obj: T): Any

  /**
    * ...
    */
  def fromGeneric(obj: Any): T

  /**
    * Writes a serialized representation of the supplied object.  Throws an
    * AvroSerializationException if writing is unsuccessful. 
    */
  @throws[AvroSerializationException[_]]
  def write(obj: T, stream: OutputStream)

  /**
    * Attempts to create an object of type T by reading the required data from
    * the supplied stream.
    */
  @throws[AvroDeserializationException[_]]
  def read(stream: InputStream): Try[T]

  /**
    * Returns the JSON serialization of the supplied object.  Throws an
    * AvroSerializationException if writing is unsuccessful. 
    */
/*
  @throws[AvroSerializationException[_]]
  def writeAsJson(obj: T): JsValue
*/

  /**
    * Attempts to create an object of type T by reading the required data from
    * the supplied JSON source.
    */
/*
  @throws[AvroDeserializationException[_]]
  def readFromJson(json: String): Try[T]
*/

}

/**
  * Companion object for [[AvroTypeIO]]
  */
object AvroTypeIO {

  import scala.language.implicitConversions

  import com.gensler.scalavro.types.primitive._
  import com.gensler.scalavro.io.primitive._

  import com.gensler.scalavro.types.complex._
  import com.gensler.scalavro.io.complex._

  /**
    * Contains implicit conversions from any AvroType to a corresponding
    * AvroTypeIO capable of reading and writing.
    *
    * Bring these members into scope to implicitly augment AvroType instances
    * with IO functionality as follows:
    *
    * {{{
    *   import com.gensler.scalavro.io.AvroTypeIO.Implicits._
    *
    *   // Given an avroType: AvroType[T] and an obj: T
    *   // it is possible to call write() directly:
    *
    *   avroType.write(obj, outputStream)
    * }}}
    */
  object Implicits {

    // primitive types
    implicit def avroTypeToIO[T](avroType: AvroPrimitiveType[T]): AvroTypeIO[T] =
      avroType match {
        case AvroBoolean => AvroBooleanIO
        case AvroBytes   => AvroBytesIO
        case AvroDouble  => AvroDoubleIO
        case AvroFloat   => AvroFloatIO
        case AvroInt     => AvroIntIO
        case AvroLong    => AvroLongIO
        case AvroNull    => AvroNullIO
        case AvroString  => AvroStringIO
      }

    // complex types
    implicit def avroTypeToIO[T](avroArray: AvroArray[T]): AvroArrayIO[T]          = AvroArrayIO(avroArray)
    implicit def avroTypeToIO[T](avroEnum: AvroEnum[T]): AvroEnumIO[T]              = AvroEnumIO(avroEnum)
    implicit def avroTypeToIO[T](avroFixed: AvroFixed[T]): AvroFixedIO[T]          = AvroFixedIO(avroFixed)
    implicit def avroTypeToIO[T](avroMap: AvroMap[T]): AvroMapIO[T]                  = AvroMapIO(avroMap)
    implicit def avroTypeToIO[T](avroError: AvroError[T]): AvroRecordIO[T]         = AvroRecordIO(avroError)
    implicit def avroTypeToIO[T](avroRecord: AvroRecord[T]): AvroRecordIO[T]      = AvroRecordIO(avroRecord)
    implicit def avroTypeToIO[A, B](avroUnion: AvroUnion[A, B]): AvroUnionIO[A, B] = AvroUnionIO(avroUnion)

    import scala.reflect.runtime.universe._

    implicit def avroTypeToIO[T: TypeTag](at: AvroType[T]): AvroTypeIO[T] = {
      at match {
        case t: AvroPrimitiveType[T] => avroTypeToIO(t)
        case t: AvroArray[T]         => avroTypeToIO(t)
        case t: AvroEnum[T]          => avroTypeToIO(t)
        case t: AvroFixed[T]         => avroTypeToIO(t)
        case t: AvroMap[T]           => avroTypeToIO(t)
        case t: AvroError[T]         => avroTypeToIO(t)
        case t: AvroRecord[T]        => avroTypeToIO(t)
        case t: AvroUnion[_, _]      => avroTypeToIO(t)
      }
    }.asInstanceOf[AvroTypeIO[T]]

  }

}