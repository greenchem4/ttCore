package tterrag.core.client.handlers;

import lombok.Getter;
import tterrag.core.common.Handlers.Handler;
import tterrag.core.common.Handlers.Handler.HandlerType;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

@Handler(HandlerType.FML)
public class ClientHandler
{
    @Getter
    private static int ticksElapsed;

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event)
    {
        if (event.phase == Phase.END)
        {
            ticksElapsed++;
        }
    }

    /**
     * Use lombok getter <code>getTicksElapsed()</code>
     */
    @Deprecated
    public static int getElapsedTicks()
    {
        return ticksElapsed;
    }
}
