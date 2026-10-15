-- Final R8 DEX audit: map the returned caller via THIS APK's mapping.txt.
-- A matching invoke is artifact evidence, not API 101/102 runtime acceptance.
SELECT DISTINCT file_id, method_idx, method, callee
FROM method_calls
WHERE callee = 'Lio/github/libxposed/api/XposedInterface$HookBuilder;->setId(Ljava/lang/String;)Lio/github/libxposed/api/XposedInterface$HookBuilder;'
ORDER BY file_id, method_idx, method, callee;
