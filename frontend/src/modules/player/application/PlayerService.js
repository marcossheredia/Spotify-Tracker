export class PlayerService {
  constructor(apiClient) {
    this.apiClient = apiClient;
  }

  async getState() {
    return this.apiClient.getState();
  }

  async getQueue() {
    return this.apiClient.getQueue();
  }

  async play() {
    return this.apiClient.play();
  }

  async pause() {
    return this.apiClient.pause();
  }

  async next() {
    return this.apiClient.next();
  }

  async previous() {
    return this.apiClient.previous();
  }

  async setRepeat(repeatState) {
    return this.apiClient.setRepeat(repeatState);
  }

  async setShuffle(shuffleState) {
    return this.apiClient.setShuffle(shuffleState);
  }

  async transferPlayback(deviceId, play) {
    return this.apiClient.transferPlayback(deviceId, play);
  }
}
