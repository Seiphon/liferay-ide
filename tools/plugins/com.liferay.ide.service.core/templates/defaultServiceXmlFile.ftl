<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE service-builder PUBLIC "-//Liferay//DTD Service Builder ${version}//EN" "http://www.liferay.com/dtd/liferay-service-builder_${version_}.dtd">
<service-builder package-path="${package_path}">
	<author>${author}</author>

	<namespace>${namespace}</namespace>

	<entity local-service="true" name="Foo" remote-service="true">

		<!-- PK fields -->

		<column name="fooId" primary="true" type="long" />

		<!-- Group instance -->

		<column name="groupId" type="long" />

		<!-- Audit fields -->

		<column name="companyId" type="long" />
		<column name="userId" type="long" />
		<column name="userName" type="String" />
		<column name="createDate" type="Date" />
		<column name="modifiedDate" type="Date" />

		<!-- Other fields -->

		<column name="field1" type="String" />
		<column name="field2" type="boolean" />
		<column name="field3" type="int" />
		<column name="field4" type="Date" />
		<column name="field5" type="String" />

		<!-- Order -->

		<order by="asc">
			<order-column name="field1" />
		</order>

		<!-- Finder methods -->

		<finder name="Field2" return-type="Collection">
			<finder-column name="field2" />
		</finder>
	</entity>
</service-builder>