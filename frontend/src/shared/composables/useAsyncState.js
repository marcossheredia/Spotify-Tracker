import { readonly, ref } from "vue";

export function useAsyncState(asyncFunction, initialValue = null) {
  const data = ref(initialValue);
  const loading = ref(false);
  const error = ref(null);

  async function execute(...args) {
    loading.value = true;
    error.value = null;
    try {
      data.value = await asyncFunction(...args);
      return data.value;
    } catch (err) {
      error.value = err;
      throw err;
    } finally {
      loading.value = false;
    }
  }

  return { data: readonly(data), loading: readonly(loading), error: readonly(error), execute };
}
