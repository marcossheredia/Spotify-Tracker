const PERIOD_OPTIONS = [
  { value: "short_term", label: "Ultimas 4 semanas" },
  { value: "medium_term", label: "Ultimos 6 meses" },
  { value: "long_term", label: "Todo el tiempo" },
];

export class TopStatsPeriod {
  static SHORT_TERM = "short_term";

  static normalize(value) {
    const stringValue = String(value || "").trim();
    const found = PERIOD_OPTIONS.find((option) => option.value === stringValue);
    return found ? found.value : TopStatsPeriod.SHORT_TERM;
  }

  static options() {
    return PERIOD_OPTIONS;
  }

  static labelFor(value) {
    const normalized = TopStatsPeriod.normalize(value);
    const found = PERIOD_OPTIONS.find((option) => option.value === normalized);
    return found ? found.label : PERIOD_OPTIONS[0].label;
  }
}
