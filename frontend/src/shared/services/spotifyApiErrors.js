export function resolveSpotifyErrorMessage(error, fallbackMessage) {
  const backendMessage = error?.response?.data?.message;
  if (backendMessage) {
    return backendMessage;
  }

  if (error?.response?.status === 429) {
    const retryAfterSeconds = error?.retryAfterSeconds;
    if (retryAfterSeconds) {
      return `Spotify aplico rate limit. Reintenta en ${retryAfterSeconds}s.`;
    }
    return "Spotify aplico rate limit. Reintenta en unos segundos.";
  }

  if (error?.response?.status === 403) {
    return "Esta funcion depende de permisos o endpoints de Spotify que pueden estar restringidos en modo desarrollo.";
  }

  return fallbackMessage;
}
