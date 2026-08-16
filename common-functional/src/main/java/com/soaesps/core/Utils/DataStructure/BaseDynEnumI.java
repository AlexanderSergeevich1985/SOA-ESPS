package com.soaesps.core.Utils.DataStructure;

import java.util.Set;

/**
 * Interface for dynamic or parameterizable enumeration structures.
 */
public interface BaseDynEnumI {

    /**
     * Returns a set of all dynamic items belonging to this enumeration context.
     *
     * @return a Set containing elements implementing BaseDynEnumI
     */
    Set<BaseDynEnumI> getDynItems();

    /**
     * Returns the unique identifier of the specific enumeration item.
     *
     * @return integer ID of the item
     */
    int getItemId();

    /**
     * Returns the string name or representation of the enumeration item.
     *
     * @return string name of the item
     */
    String getItemName();
}