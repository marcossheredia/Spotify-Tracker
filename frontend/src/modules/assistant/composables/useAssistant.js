import { ref } from "vue";
import { AssistantService } from "../application/AssistantService";
import { AssistantApiClient } from "../infrastructure/AssistantApiClient";
import { resolveSpotifyErrorMessage } from "@/shared/services/spotifyApiErrors";

const defaultAssistantService = new AssistantService(new AssistantApiClient());

function resolveErrorMessage(error) {
  return (
    error?.response?.data?.message ||
    resolveSpotifyErrorMessage(error, "No se pudo crear la playlist.")
  );
}

export function useAssistant(assistantService = defaultAssistantService) {
  const phase = ref("idle");
  const isLoading = ref(false);
  const error = ref("");
  const lastResult = ref(null);

  async function createPlaylist(message, options = {}) {
    const safeMessage = String(message || "").trim();
    if (!safeMessage || isLoading.value) {
      return null;
    }

    isLoading.value = true;
    error.value = "";
    phase.value = "thinking";
    lastResult.value = null;

    try {
      phase.value = "creating";
      const result = await assistantService.createPlaylist(safeMessage, options);
      lastResult.value = result;
      phase.value = "success";
      return result;
    } catch (requestError) {
      error.value = resolveErrorMessage(requestError);
      phase.value = "error";
      return null;
    } finally {
      isLoading.value = false;
    }
  }

  return {
    phase,
    isLoading,
    error,
    lastResult,
    createPlaylist,
  };
}
