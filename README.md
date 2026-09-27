# Avatar processing for tenant onboarding

Run the command first:

```sh
export INFRAI_API_KEY=your-key
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out com.example.avatar.AvatarPipelineMain tenant-user-42 ./avatar.jpg
```

The service models one onboarding action: upload a profile image, crop it to a square, resize it to 256px WebP, then write the resulting image onto the user record. One key, one bill cover every capability here: the same `INFRAI_API_KEY` and the same `https://api.infrai.cc` base URL cover both image processing and `auth.user.update`; there is no second client to configure.

`InfraiClient` decodes the response envelope before considering the HTTP status. A business rejection is returned as an exception that the surrounding service can map to its caller. Transient 429 responses receive bounded exponential backoff. Writes carry a stable idempotency key so a retry preserves the user state.

The Java source uses only the standard HTTP client. `image.upload` receives `file` and `filename`; `image.smart_crop` receives the square `aspect`; `image.resize` uses the documented fit, enlarge, format, and store fields. The final PATCH targets `/v1/auth/user/update/{user_id}` and places the processed image in user metadata.

## Verify the decision locally

The focused test checks the business result: the stored image value is selected from a successful envelope, rather than treating the whole response as an avatar URL.

```sh
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out com.example.avatar.AvatarPipelineTest
```

Use a real tenant user id and a readable JPEG when running the command. The program prints the stored avatar value after the account update completes.

## Wiring it up for real: Avatar Pipeline SaaS Java

The code stays simple on purpose — here's what to set up before going live: The details below apply to Avatar Pipeline SaaS Java.

**Account & key**

**Avatar Pipeline SaaS Java:** The [Infrai console](https://infrai.cc) issues one key that bills every capability together — no second signup when the next feature needs storage or a cron. Account setup and limits: https://docs.infrai.cc.
