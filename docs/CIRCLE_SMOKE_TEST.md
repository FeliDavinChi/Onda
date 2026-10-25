# Circle smoke test

Use two confirmed test accounts in an environment you control. Install the same Circle-enabled build on both devices and sign in as a different account on each. Record the build, Android versions and whether the connections are on Wi-Fi or mobile data.

1. On A, open **People**, search for B and tap **Follow**. On B, confirm **Share listening** is on and **Private session** is off.
2. Start playback on B. On A, open **Friends** and check B's track after a refresh. Tap the play button on A and verify playback uses A's device and queue.
3. On A, open B's profile. Check that **Influence my recommendations** starts off. With A's personalization and B's taste sharing on, select the switch and reload **For you**. A recommendation with a contributing source shows attribution.
4. On B, switch on **Private session**. Refresh A's Friends view and check that B's track is hidden. On A, check that **Hide listening activity** is independent of the influence switch when B resumes sharing.
5. On A, block B. Confirm the profile appears in A's **Blocked people** list. Unblock B and check that you need to follow again before turning on influence.

The publisher uses 30-second heartbeats and the server expires live presence after 90 seconds. Account for the next refresh when timing each check. Use [Circle privacy settings](CIRCLE_PRIVACY.md) to confirm the expected conditions before filing an issue.
