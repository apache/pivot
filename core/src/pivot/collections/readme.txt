 The primary advantages of VMware collections over Sun collections are:

 - Minimal interfaces that provide all necessary operations but are simpler
   to implement, including "lightweight" versions suitable for combining with
   other collection interfaces

 - Support for for..each in all collections, including Map (enumeration similar
   to JavaScript Object properties)

 - Change listeners, which allow callers to be notified when collections are
   modified

 - Auto-sorting for all collections

 - Built-in support for nested collections (i.e. tree data)

 - Support for collections of unknown size (e.g. for deserialization of data
    such as JDBC result sets, XML data streams, etc.)

