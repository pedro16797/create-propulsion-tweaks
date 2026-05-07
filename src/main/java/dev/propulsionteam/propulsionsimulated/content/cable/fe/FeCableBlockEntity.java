package dev.propulsionteam.propulsionsimulated.content.cable.fe;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.propulsionteam.propulsionsimulated.PropulsionConfig;
import dev.propulsionteam.propulsionsimulated.content.cable.hub.CableHubBlockEntity;
import dev.propulsionteam.propulsionsimulated.registries.PropulsionBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.*;

public class FeCableBlockEntity extends SmartBlockEntity {
    private final IEnergyStorage storage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return 0;
        }

        @Override
        public int getMaxEnergyStored() {
            return 0;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    };

    public FeCableBlockEntity(BlockPos pos, BlockState blockState) {
        super(PropulsionBlockEntities.FE_CABLE_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) {
            return;
        }

        Set<BlockPos> network = collectNetwork(worldPosition);
        if (network.isEmpty()) {
            return;
        }

        BlockPos controller = network.stream().min(Comparator
            .comparingInt((BlockPos p) -> p.getY())
            .thenComparingInt(p -> p.getX())
            .thenComparingInt(p -> p.getZ())).orElse(worldPosition);

        if (!worldPosition.equals(controller)) {
            return;
        }

        routeNetworkEnergy(network);
    }

    private void routeNetworkEnergy(Set<BlockPos> network) {
        List<Endpoint> sources = new ArrayList<>();
        List<Endpoint> sinks = new ArrayList<>();

        for (BlockPos cablePos : network) {
            BlockState state = level.getBlockState(cablePos);
            for (Direction direction : Direction.values()) {
                if (!FeCableBlock.isSideEnabled(state, direction) || !FeCableBlock.isSideConnected(state, direction)) {
                    continue;
                }

                BlockPos neighborPos = cablePos.relative(direction);
                if (isTransitNode(neighborPos)) {
                    continue;
                }

                IEnergyStorage cap = level.getCapability(Capabilities.EnergyStorage.BLOCK, neighborPos, direction.getOpposite());
                if (cap == null) {
                    continue;
                }

                Endpoint endpoint = new Endpoint(cablePos, direction, cap);
                if (cap.canExtract()) {
                    sources.add(endpoint);
                }
                if (cap.canReceive()) {
                    sinks.add(endpoint);
                }
            }
        }

        if (sources.isEmpty() || sinks.isEmpty()) {
            return;
        }

        sources.sort(Endpoint::compare);
        sinks.sort(Endpoint::compare);

        int budget = Math.max(0, getTransferPerTick()) * network.size();
        if (budget <= 0) {
            return;
        }

        boolean madeProgress;
        do {
            madeProgress = false;

            int activeSinkCount = 0;
            for (Endpoint sink : sinks) {
                int sinkWant = sink.storage.receiveEnergy(Math.max(1, budget / Math.max(1, sinks.size())), true);
                if (sinkWant > 0) {
                    activeSinkCount++;
                }
            }
            if (activeSinkCount <= 0) {
                break;
            }

            int baseShare = Math.max(1, budget / activeSinkCount);
            int remainder = budget % activeSinkCount;

            for (Endpoint sink : sinks) {
                if (budget <= 0) {
                    break;
                }

                int request = baseShare;
                if (remainder > 0) {
                    request++;
                    remainder--;
                }
                request = Math.min(request, budget);

                int canAccept = sink.storage.receiveEnergy(request, true);
                if (canAccept <= 0) {
                    continue;
                }

                int extracted = extractFromSources(sources, canAccept);
                if (extracted <= 0) {
                    continue;
                }

                int accepted = sink.storage.receiveEnergy(extracted, false);
                if (accepted <= 0) {
                    continue;
                }

                budget -= accepted;
                madeProgress = true;
            }
        } while (budget > 0 && madeProgress);
    }

    private int extractFromSources(List<Endpoint> sources, int needed) {
        int remaining = needed;
        int totalExtracted = 0;

        for (Endpoint source : sources) {
            if (remaining <= 0) {
                break;
            }
            int available = source.storage.extractEnergy(remaining, true);
            if (available <= 0) {
                continue;
            }
            int extracted = source.storage.extractEnergy(available, false);
            if (extracted <= 0) {
                continue;
            }
            totalExtracted += extracted;
            remaining -= extracted;
        }

        return totalExtracted;
    }

    private Set<BlockPos> collectNetwork(BlockPos start) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            if (!visited.add(current)) {
                continue;
            }

            BlockState state = level.getBlockState(current);
            if (!(state.getBlock() instanceof FeCableBlock)) {
                continue;
            }

            for (Direction direction : Direction.values()) {
                if (!FeCableBlock.isSideEnabled(state, direction) || !FeCableBlock.isSideConnected(state, direction)) {
                    continue;
                }

                BlockPos neighbor = current.relative(direction);
                var be = level.getBlockEntity(neighbor);
                if (!(be instanceof FeCableBlockEntity)) {
                    continue;
                }

                BlockState neighborState = level.getBlockState(neighbor);
                if (!FeCableBlock.isSideEnabled(neighborState, direction.getOpposite())
                    || !FeCableBlock.isSideConnected(neighborState, direction.getOpposite())) {
                    continue;
                }

                if (!visited.contains(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }

        return visited;
    }

    private boolean isTransitNode(BlockPos pos) {
        var be = level != null ? level.getBlockEntity(pos) : null;
        return be instanceof FeCableBlockEntity || be instanceof CableHubBlockEntity;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    public IEnergyStorage getEnergyHandler(Direction side) {
        return storage;
    }

    private static int getTransferPerTick() {
        return PropulsionConfig.CABLE_ENERGY_TRANSFER.get();
    }

    private record Endpoint(BlockPos cablePos, Direction direction, IEnergyStorage storage) {
        private static int compare(Endpoint a, Endpoint b) {
            int byY = Integer.compare(a.cablePos.getY(), b.cablePos.getY());
            if (byY != 0) return byY;
            int byX = Integer.compare(a.cablePos.getX(), b.cablePos.getX());
            if (byX != 0) return byX;
            int byZ = Integer.compare(a.cablePos.getZ(), b.cablePos.getZ());
            if (byZ != 0) return byZ;
            return Integer.compare(a.direction.ordinal(), b.direction.ordinal());
        }
    }
}
