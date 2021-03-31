package team.cappcraft.immersivechemical.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageBlockEvent implements IMessage {
    BlockPos pos;
    int id;
    int param;

    public MessageBlockEvent() {
    }

    public MessageBlockEvent(BlockPos pos, int id, int param) {
        this.pos = pos;
        this.id = id;
        this.param = param;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        this.id = buf.readInt();
        this.param = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pos.getX()).writeInt(pos.getY()).writeInt(pos.getZ());
        buf.writeInt(id);
        buf.writeInt(param);
    }

    public static class HandlerServer implements IMessageHandler<MessageBlockEvent, IMessage> {
        @Override
        public IMessage onMessage(MessageBlockEvent message, MessageContext ctx) {
            WorldServer world = ctx.getServerHandler().player.getServerWorld();
            world.addBlockEvent(message.pos, world.getBlockState(message.pos).getBlock(), message.id, message.param);
            return null;
        }
    }
}
