package uml2shacl.lib

package object transformer {
  /**
   * Utility function to sanitize GUID texts of the form "GUID ae5e3720-4e3e-40f1-9346-9a8b4e501f35" so that they can be used in URIs.
   */
  def sanitizeGuid(guid: String): String =
    guid.replace(' ', '_')
}
