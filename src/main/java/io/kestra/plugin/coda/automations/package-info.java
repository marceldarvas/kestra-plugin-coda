/**
 * Tasks for triggering Coda automations via webhooks.
 *
 * <p>This package provides functionality to trigger Coda automations from Kestra workflows.
 * Webhooks allow external applications to invoke actions in Coda documents programmatically.</p>
 *
 * <h2>Features:</h2>
 * <ul>
 *   <li>Trigger webhook-based automations</li>
 *   <li>Send custom payloads to Coda automations</li>
 *   <li>Support for various data types (strings, numbers, booleans, arrays, objects)</li>
 * </ul>
 *
 * @see <a href="https://coda.io/developers">Coda API Documentation</a>
 */
@PluginSubGroup(
    title = "Automations",
    description = "Tasks for triggering Coda automations via webhooks",
    categories = PluginSubGroup.PluginCategory.TOOL
)
package io.kestra.plugin.coda.automations;

import io.kestra.core.models.annotations.PluginSubGroup;
