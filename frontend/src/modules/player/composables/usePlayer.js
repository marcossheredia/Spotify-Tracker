import { ref } from "vue";
import { PlayerService } from "../application/PlayerService";
import { SpotifyPlayerApiClient } from "../infrastructure/SpotifyPlayerApiClient";
import { resolveSpotifyErrorMessage } from "@/shared/services/spotifyApiErrors";

const defaultService = new PlayerService(new SpotifyPlayerApiClient());

export function usePlayer(playerService = defaultService) {
  const state = ref(null);
  const queue = ref({ currentlyPlaying: null, queue: [] });
  const loading = ref(false);
  const actionLoading = ref(false);
  const error = ref("");
  const actionMessage = ref("");

  async function loadState() {
    loading.value = true;
    error.value = "";

    try {
      state.value = await playerService.getState();
    } catch (requestError) {
      state.value = null;
      error.value = resolveSpotifyErrorMessage(requestError, "No se pudo cargar el estado del reproductor.");
    } finally {
      loading.value = false;
    }
  }

  async function loadQueue() {
    try {
      queue.value = await playerService.getQueue();
    } catch (requestError) {
      error.value = resolveSpotifyErrorMessage(requestError, "No se pudo cargar la cola de reproduccion.");
      queue.value = { currentlyPlaying: null, queue: [] };
    }
  }

  async function runAction(action) {
    actionLoading.value = true;
    error.value = "";
    actionMessage.value = "";

    try {
      const result = await action();
      actionMessage.value = result?.message || "Accion ejecutada.";
      await loadState();
      return result;
    } catch (requestError) {
      error.value = resolveSpotifyErrorMessage(requestError, "No se pudo completar la accion del reproductor.");
      return null;
    } finally {
      actionLoading.value = false;
    }
  }

  return {
    state,
    queue,
    loading,
    actionLoading,
    error,
    actionMessage,
    loadState,
    loadQueue,
    runAction,
    actions: {
      play: () => runAction(() => playerService.play()),
      pause: () => runAction(() => playerService.pause()),
      next: () => runAction(() => playerService.next()),
      previous: () => runAction(() => playerService.previous()),
      setRepeat: (stateValue) => runAction(() => playerService.setRepeat(stateValue)),
      setShuffle: (enabled) => runAction(() => playerService.setShuffle(enabled)),
      transferPlayback: (deviceId, play = false) =>
        runAction(() => playerService.transferPlayback(deviceId, play)),
    },
  };
}
