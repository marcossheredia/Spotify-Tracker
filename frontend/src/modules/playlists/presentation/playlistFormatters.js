export function formatPlayedAt(playedAt) {
  const date = new Date(playedAt);
  if (Number.isNaN(date.getTime())) {
    return "recientemente";
  }

  return date.toLocaleString("es-ES", {
    day: "2-digit",
    month: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
}