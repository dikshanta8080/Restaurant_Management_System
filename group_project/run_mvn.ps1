mvn exec:java -Dexec.mainClass="com.dikshanta.restaurant.management.system.group_project.ApiManualTest" -Dexec.classpathScope=test 2>&1 | Out-File -FilePath .\mvn_output.log -Encoding utf8
