module hobo.api.hoboplugin;

/** 
The base for any Hobo plugin.
*/
abstract class HoboPlugin
{
    /** 
    The name of the plugin.
    Returns: The name of the plugin.
    */
    abstract string name() const;
}
