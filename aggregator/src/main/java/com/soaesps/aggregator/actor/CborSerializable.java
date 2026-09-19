package com.soaesps.aggregator.actor;

/**
 * Marker trait for serialization messages across the Pekko Cluster Sharding network.
 * Any command or record passed to Sharding must implement this interface to leverage CBOR compression.
 */
public interface CborSerializable {
}