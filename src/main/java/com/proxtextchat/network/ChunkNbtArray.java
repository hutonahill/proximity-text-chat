package com.proxtextchat.network;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtString;
import com.proxtextchat.util.Tuple;

import java.util.Collection;

public class ChunkNbtArray{
    private final long[] location;
    private final String LocationKey = "Location";

    private final String[] dimension;
    private final String DimensionKey = "Dimension";

    // Constructor to initialize the arrays
    public ChunkNbtArray(int size) {
        location = new long[size];
        dimension = new String[size];
    }

    public ChunkNbtArray(NbtCompound nbt) {
        // Retrieve the long array from NBT
        location = nbt.getLongArray(LocationKey);

        // Retrieve the string list from NBT
        NbtList stringList = nbt.getList(DimensionKey, 8);
        dimension = new String[stringList.size()];

        // Populate the dimension array
        for (int i = 0; i < stringList.size(); i++) {
            dimension[i] = stringList.getString(i);
        }
    }

    // Set a tuple at the given index
    public void set(int index, long loc, String dim) {


        if (index < 0 || index >= location.length) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }
        location[index] = loc;
        dimension[index] = dim;
    }

    // Get the tuple (long, String) at a specific index
    public Tuple<Long, String> get(int index) {
        if (index < 0 || index >= location.length) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }
        return new Tuple<>(location[index], dimension[index]);
    }

    // Allow access with bracket-like syntax via a get method
    public Tuple<Long, String> getAt(int index) {
        return get(index);
    }

    // Return the long array (locations)
    public long[] getLocationArray() {
        return location;
    }

    // Return the String array (dimensions)
    public String[] getDimensionArray() {
        return dimension;
    }

    // Method to add this array data to an NBT compound and return the modified NBT compound
    public NbtCompound toNBT(NbtCompound nbt) {

        NbtList StringArray = new NbtList();

        for (String str : dimension){
            StringArray.add(NbtString.of(str));
        }

        nbt.put(DimensionKey, StringArray);
        nbt.putLongArray(LocationKey, location);

        return nbt;
    }

    // Allow iteration over the ChunkNbtArray with a for-each loop
    public Iterable<Tuple<Long, String>> iterable() {
        return () -> new java.util.Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < location.length;
            }

            @Override
            public Tuple<Long, String> next() {
                return getAt(index++);
            }
        };
    }
}
