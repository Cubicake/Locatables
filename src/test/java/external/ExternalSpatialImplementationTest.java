package external;

import games.cubi.locatables.api.ImmutableBlockSpatial;
import games.cubi.locatables.api.ImmutableSpatial;
import games.cubi.locatables.api.MutableBlockSpatial;
import games.cubi.locatables.api.MutableLocatable;
import games.cubi.locatables.api.MutableSpatial;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExternalSpatialImplementationTest {
    @Test
    void externalImplementorsChooseAnExplicitLeaf() {
        ImmutableSpatial immutable = new ExternalImmutable(1, 2, 3);
        MutableSpatial mutable = new ExternalMutable(1, 2, 3);
        ImmutableBlockSpatial immutableBlock = new ExternalImmutableBlock(1, 2, 3);
        MutableBlockSpatial mutableBlock = new ExternalMutableBlock(1, 2, 3);

        assertEquals(1, immutable.x());
        assertEquals(1, mutable.x());
        assertEquals(1, immutableBlock.blockX());
        assertEquals(1, mutableBlock.blockX());
    }

    @Test
    void legacyMutableLocatableUsesDefaultBulkMutation() {
        UUID world = UUID.randomUUID();
        LegacyMutableLocatable locatable = new LegacyMutableLocatable(world, 1, 2, 3);

        MutableLocatable result = locatable.setPosition(4, 5, 6);

        assertEquals(locatable, result);
        assertEquals(3, locatable.componentUpdates);
        assertEquals(4, locatable.x());
        assertEquals(5, locatable.y());
        assertEquals(6, locatable.z());
    }

    private record ExternalImmutable(double x, double y, double z) implements ImmutableSpatial {
    }

    private record ExternalImmutableBlock(int blockX, int blockY, int blockZ) implements ImmutableBlockSpatial {
    }

    private static class ExternalMutable implements MutableSpatial {
        private double x;
        private double y;
        private double z;

        private ExternalMutable(double x, double y, double z) {
            setPosition(x, y, z);
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
        public ExternalMutable setX(double x) {
            this.x = x;
            return this;
        }

        @Override
        public ExternalMutable setY(double y) {
            this.y = y;
            return this;
        }

        @Override
        public ExternalMutable setZ(double z) {
            this.z = z;
            return this;
        }

        @Override
        public ExternalMutable setPosition(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }
    }

    private static final class ExternalMutableBlock implements MutableBlockSpatial {
        private int x;
        private int y;
        private int z;

        private ExternalMutableBlock(int x, int y, int z) {
            setBlockPosition(x, y, z);
        }

        @Override
        public int blockX() {
            return x;
        }

        @Override
        public int blockY() {
            return y;
        }

        @Override
        public int blockZ() {
            return z;
        }

        @Override
        public ExternalMutableBlock setBlockX(int x) {
            this.x = x;
            return this;
        }

        @Override
        public ExternalMutableBlock setBlockY(int y) {
            this.y = y;
            return this;
        }

        @Override
        public ExternalMutableBlock setBlockZ(int z) {
            this.z = z;
            return this;
        }

        @Override
        public ExternalMutableBlock setBlockPosition(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }
    }

    /** Models an existing consumer implementation that predates setPosition. */
    private static final class LegacyMutableLocatable implements MutableLocatable {
        private UUID world;
        private double x;
        private double y;
        private double z;
        private int componentUpdates;

        private LegacyMutableLocatable(UUID world, double x, double y, double z) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public UUID world() {
            return world;
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
        public LegacyMutableLocatable setX(double x) {
            componentUpdates++;
            this.x = x;
            return this;
        }

        @Override
        public LegacyMutableLocatable setY(double y) {
            componentUpdates++;
            this.y = y;
            return this;
        }

        @Override
        public LegacyMutableLocatable setZ(double z) {
            componentUpdates++;
            this.z = z;
            return this;
        }

        @Override
        public LegacyMutableLocatable setWorld(UUID world) {
            this.world = world;
            return this;
        }

        @Override
        public LocatableType getType() {
            return LocatableType.ExternalMutable;
        }

        @Override
        public boolean strictlyEquals(Object other) {
            if (this == other) return true;
            if (!(other instanceof LegacyMutableLocatable that)) return false;
            return world.equals(that.world)
                    && Double.doubleToLongBits(x) == Double.doubleToLongBits(that.x)
                    && Double.doubleToLongBits(y) == Double.doubleToLongBits(that.y)
                    && Double.doubleToLongBits(z) == Double.doubleToLongBits(that.z);
        }
    }
}
