package games.cubi.locatables.api;

import games.cubi.locatables.implementations.ImmutableBlockLocatable;
import games.cubi.locatables.implementations.ImmutableLocatableImpl;
import games.cubi.locatables.implementations.MutableBlockSpatialImpl;
import games.cubi.locatables.implementations.MutableBlockVector;
import games.cubi.locatables.implementations.MutableLocatableImpl;
import games.cubi.locatables.implementations.MutableSpatialImpl;
import games.cubi.locatables.implementations.ThreadSafeLocatable;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpatialApiTest {
    @Test
    void projectsNegativeCoordinatesToBlocksAndChunks() {
        Spatial spatial = new ImmutableTestSpatial(-0.1, -16.0, -16.1);

        assertEquals(-1, spatial.blockX());
        assertEquals(-16, spatial.blockY());
        assertEquals(-17, spatial.blockZ());
        assertEquals(-1, spatial.chunkX());
        assertEquals(-1, spatial.chunkY());
        assertEquals(-2, spatial.chunkZ());
    }

    @Test
    void blockSquaredDistanceUsesLongArithmetic() {
        BlockSpatial first = new ImmutableTestBlockSpatial(30_000_000, 30_000_000, 30_000_000);
        BlockSpatial second = new ImmutableTestBlockSpatial(-30_000_000, -30_000_000, -30_000_000);

        assertEquals(10_800_000_000_000_000L, first.blockDistanceSquared(second));
        assertEquals(Math.sqrt(10_800_000_000_000_000L), first.blockDistance(second));
    }

    @Test
    void centreMutatesContinuousMutableSpatials() {
        MutableSpatialImpl spatial = new MutableSpatialImpl(-1.2, 3.9, 4.0);

        MutableSpatial centred = spatial.centre();

        assertSame(spatial, centred);
        assertCoordinates(centred, -1.5, 3.5, 4.5);
    }

    @Test
    void centreAllocatesForImmutableAndBlockSpatials() {
        ImmutableSpatial immutable = new ImmutableTestSpatial(-1.2, 3.9, 4.0);
        MutableBlockSpatialImpl block = new MutableBlockSpatialImpl(-2, 3, 4);

        MutableSpatial immutableCentre = immutable.centre();
        MutableSpatial blockCentre = block.centre();

        assertNotSame(immutable, immutableCentre);
        assertNotSame(block, blockCentre);
        assertFalse(immutableCentre instanceof BlockSpatial);
        assertFalse(blockCentre instanceof BlockSpatial);
        assertCoordinates(immutableCentre, -1.5, 3.5, 4.5);
        assertCoordinates(blockCentre, -1.5, 3.5, 4.5);
        assertEquals(-2, block.blockX());
        assertEquals(3, block.blockY());
        assertEquals(4, block.blockZ());
    }

    @Test
    void locatableCentrePreservesWorldAndIdentityRules() {
        UUID world = UUID.randomUUID();
        MutableLocatableImpl mutable = new MutableLocatableImpl(world, -1.2, 3.9, 4.0);
        ImmutableLocatableImpl immutable = new ImmutableLocatableImpl(world, -1.2, 3.9, 4.0);
        ImmutableBlockLocatable block = new ImmutableBlockLocatable(world, -2, 3, 4);
        games.cubi.locatables.implementations.MutableBlockLocatable mutableBlock =
                new games.cubi.locatables.implementations.MutableBlockLocatable(world, -2, 3, 4);

        MutableLocatable mutableCentre = mutable.centre();
        MutableLocatable immutableCentre = immutable.centre();
        MutableLocatable blockCentre = block.centre();
        MutableLocatable mutableBlockCentre = mutableBlock.centre();

        assertSame(mutable, mutableCentre);
        assertNotSame(immutable, immutableCentre);
        assertNotSame(block, blockCentre);
        assertNotSame(mutableBlock, mutableBlockCentre);
        assertEquals(world, mutableCentre.world());
        assertEquals(world, immutableCentre.world());
        assertEquals(world, blockCentre.world());
        assertEquals(world, mutableBlockCentre.world());
        assertFalse(blockCentre instanceof BlockSpatial);
        assertFalse(mutableBlockCentre instanceof BlockSpatial);
        assertCoordinates(mutableCentre, -1.5, 3.5, 4.5);
        assertCoordinates(immutableCentre, -1.5, 3.5, 4.5);
        assertCoordinates(blockCentre, -1.5, 3.5, 4.5);
        assertCoordinates(mutableBlockCentre, -1.5, 3.5, 4.5);
    }

    @Test
    void threadSafeCentreUsesOneInPlaceMutation() {
        UUID world = UUID.randomUUID();
        ThreadSafeLocatable locatable = new ThreadSafeLocatable(world, -1.2, 3.9, 4.0);

        ThreadSafeLocatable centred = locatable.centre();

        assertSame(locatable, centred);
        assertEquals(world, centred.world());
        assertCoordinates(centred, -1.5, 3.5, 4.5);
    }

    @Test
    void vectorOperationsUseOneBulkMutation() {
        CountingMutableSpatial mutable = new CountingMutableSpatial(1, 2, 3);

        mutable.add(new ImmutableTestSpatial(4, 5, 6));

        assertEquals(1, mutable.bulkUpdates);
        assertEquals(0, mutable.componentUpdates);
        assertCoordinates(mutable, 5, 7, 9);
    }

    @Test
    void zeroVectorCannotBeNormalized() {
        MutableSpatialImpl mutableSpatial = new MutableSpatialImpl(0, 0, 0);
        MutableLocatable mutableLocatable = new MutableLocatableImpl(UUID.randomUUID(), 0, 0, 0);

        assertThrows(ArithmeticException.class, mutableSpatial::normalize);
        assertThrows(ArithmeticException.class, mutableLocatable::normalize);
    }

    @Test
    void mutableBlockSpatialFloorsContinuousInput() {
        MutableBlockSpatialImpl mutable = new MutableBlockSpatialImpl(0, 0, 0);

        mutable.setPosition(-0.1, 2.9, -16.01);

        assertEquals(-1, mutable.blockX());
        assertEquals(2, mutable.blockY());
        assertEquals(-17, mutable.blockZ());
    }

    @Test
    void mutableLocatableRestoresLegacyAndNewMutationSurfaces() {
        UUID firstWorld = UUID.randomUUID();
        UUID secondWorld = UUID.randomUUID();
        MutableLocatable mutable = new MutableLocatableImpl(firstWorld, 0, 0, 0);

        mutable.setX(1).setY(2).setZ(3);
        assertCoordinates(mutable, 1, 2, 3);

        mutable.set(4.0, 5.0, 6.0);
        assertCoordinates(mutable, 4, 5, 6);

        mutable.set(7, 8, 9);
        assertCoordinates(mutable, 7, 8, 9);

        mutable.set(1, 2, 3, secondWorld);
        assertEquals(secondWorld, mutable.world());
        assertCoordinates(mutable, 1, 2, 3);

        Locatable source = new ImmutableLocatableImpl(firstWorld, 4, 5, 6);
        mutable.set(source);
        assertEquals(firstWorld, mutable.world());
        assertCoordinates(mutable, 4, 5, 6);

        mutable.setPosition(new ImmutableTestSpatial(1, 2, 3));
        mutable.add(source);
        assertCoordinates(mutable, 5, 7, 9);

        mutable.add((Spatial) new ImmutableTestSpatial(1, 1, 1));
        mutable.add(1, 2, 3);
        assertCoordinates(mutable, 7, 10, 13);

        mutable.subtract(source);
        mutable.subtract((Spatial) new ImmutableTestSpatial(1, 1, 1));
        assertCoordinates(mutable, 2, 4, 6);

        mutable.scalarMultiply(0.5);
        assertCoordinates(mutable, 1, 2, 3);

        mutable.set(3, 0, 0).normalize();
        assertCoordinates(mutable, 1, 0, 0);

        mutable.setLocation(secondWorld, 8, 9, 10);
        assertEquals(secondWorld, mutable.world());
        assertCoordinates(mutable, 8, 9, 10);
    }

    @Test
    void threadSafeCompoundOperationsUsePrivateAtomicState() {
        UUID sourceWorld = UUID.randomUUID();
        ThreadSafeLocatable source = new ThreadSafeLocatable(sourceWorld, 1, 2, 3);
        ThreadSafeLocatable target = new ThreadSafeLocatable(UUID.randomUUID(), 4, 5, 6);

        target.add(source).subtract(source).setPosition(source);
        assertCoordinates(target, 1, 2, 3);

        target.setLocation(source);
        assertEquals(sourceWorld, target.world());
        assertCoordinates(target, 1, 2, 3);
    }

    @Test
    void equalityPoliciesAreSymmetricAndSeparate() {
        UUID world = UUID.randomUUID();
        ImmutableTestSpatial immutableSpatial = new ImmutableTestSpatial(1, 2, 3);
        MutableSpatialImpl mutableSpatial = new MutableSpatialImpl(1, 2, 3);
        ImmutableLocatableImpl immutableLocatable = new ImmutableLocatableImpl(world, 1, 2, 3);
        MutableLocatableImpl mutableLocatable = new MutableLocatableImpl(world, 1, 2, 3);
        ImmutableBlockLocatable immutableBlock = new ImmutableBlockLocatable(world, 1, 2, 3);
        games.cubi.locatables.implementations.MutableBlockLocatable mutableBlock =
                new games.cubi.locatables.implementations.MutableBlockLocatable(world, 1, 2, 3);
        MutableBlockVector blockVector = new MutableBlockVector(world, 1.9, 2.1, 3.8);

        assertEquals(immutableSpatial, mutableSpatial);
        assertEquals(mutableSpatial, immutableSpatial);
        assertEquals(immutableSpatial.hashCode(), mutableSpatial.hashCode());

        assertEquals(immutableLocatable, mutableLocatable);
        assertEquals(mutableLocatable, immutableLocatable);
        assertEquals(immutableLocatable.hashCode(), mutableLocatable.hashCode());

        assertEquals(immutableBlock, mutableBlock);
        assertEquals(mutableBlock, immutableBlock);
        assertEquals(immutableBlock, blockVector);
        assertEquals(blockVector, immutableBlock);
        assertEquals(immutableBlock.hashCode(), blockVector.hashCode());

        assertNotEquals(immutableSpatial, immutableLocatable);
        assertNotEquals(immutableLocatable, immutableSpatial);
        assertNotEquals(immutableLocatable, immutableBlock);
        assertNotEquals(immutableBlock, immutableLocatable);
    }

    @Test
    void locatablesAreSpatialsAndMutabilityIsRepresentedByType() {
        UUID world = UUID.randomUUID();
        Locatable immutable = new ImmutableLocatableImpl(world, 1, 2, 3);
        Locatable mutable = new ThreadSafeLocatable(world, 1, 2, 3);

        assertInstanceOf(Spatial.class, immutable);
        assertFalse(immutable instanceof MutableSpatial);
        assertInstanceOf(MutableSpatial.class, mutable);
        assertFalse(mutable instanceof MutableFloatingSpatial);
        assertTrue(Spatial.class.isSealed());
        assertTrue(Locatable.class.isSealed());
        assertTrue(BlockSpatial.class.isSealed());
        assertTrue(BlockLocatable.class.isSealed());
    }

    private static void assertCoordinates(Spatial spatial, double x, double y, double z) {
        assertEquals(x, spatial.x());
        assertEquals(y, spatial.y());
        assertEquals(z, spatial.z());
    }

    private record ImmutableTestSpatial(double x, double y, double z)
            implements ImmutableSpatial, FloatingSpatialEquality {
        @Override
        public boolean equals(Object other) {
            return isEqualTo(other);
        }

        @Override
        public int hashCode() {
            return makeHash();
        }
    }

    private record ImmutableTestBlockSpatial(int blockX, int blockY, int blockZ)
            implements ImmutableBlockSpatial {
    }

    private static final class CountingMutableSpatial implements MutableFloatingSpatial {
        private double x;
        private double y;
        private double z;
        private int componentUpdates;
        private int bulkUpdates;

        private CountingMutableSpatial(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public double x() {
            return x;
        }

        @Override
        public double y() {
            return y;
        }

        @Override
        public double z() {
            return z;
        }

        @Override
        public CountingMutableSpatial setX(double x) {
            componentUpdates++;
            this.x = x;
            return this;
        }

        @Override
        public CountingMutableSpatial setY(double y) {
            componentUpdates++;
            this.y = y;
            return this;
        }

        @Override
        public CountingMutableSpatial setZ(double z) {
            componentUpdates++;
            this.z = z;
            return this;
        }

        @Override
        public CountingMutableSpatial setPosition(double x, double y, double z) {
            bulkUpdates++;
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }
    }
}
